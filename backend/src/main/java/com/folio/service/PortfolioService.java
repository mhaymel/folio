package com.folio.service;

import com.folio.dto.*;
import com.folio.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final EntityManager em;
    private final SettingRepository settingRepo;

    public PortfolioService(EntityManager em, SettingRepository settingRepo) {
        this.em = em;
        this.settingRepo = settingRepo;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> getStocks() {
        // Get open positions: ISIN + depot with SUM(count) > 0
        List<Tuple> positions = em.createQuery("""
            SELECT t.isin.id as isinId, t.isin.isin as isinCode,
                   t.depot.name as depotName,
                   SUM(t.count) as totalCount,
                   SUM(t.count * t.sharePrice) as totalCost
            FROM TransactionEntity t
            GROUP BY t.isin.id, t.isin.isin, t.depot.name
            HAVING SUM(t.count) > 0
            """, Tuple.class).getResultList();

        List<StockDto> result = new ArrayList<>();
        for (Tuple pos : positions) {
            Integer isinId = pos.get("isinId", Integer.class);
            String isinCode = pos.get("isinCode", String.class);
            String depotName = pos.get("depotName", String.class);
            Double totalCount = pos.get("totalCount", Double.class);
            Double totalCost = pos.get("totalCost", Double.class);

            double avgEntryPrice = totalCount > 0 ? totalCost / totalCount : 0;

            // Get name
            String name = getFirstName(isinId);
            // Get country
            String country = getCountry(isinId);
            // Get branch
            String branch = getBranch(isinId);
            // Get quote
            Double currentQuote = getQuote(isinId);
            // Get dividend per share
            Double dps = getDividendPerShare(isinId);

            Double performancePercent = null;
            if (currentQuote != null && avgEntryPrice > 0) {
                performancePercent = (currentQuote - avgEntryPrice) / avgEntryPrice * 100;
            }

            Double estimatedAnnualIncome = (dps != null && dps > 0) ? dps * totalCount : null;

            result.add(StockDto.builder()
                .isin(isinCode).name(name).country(country).branch(branch)
                .depot(depotName).count(totalCount).avgEntryPrice(avgEntryPrice)
                .currentQuote(currentQuote).performancePercent(performancePercent)
                .dividendPerShare(dps).estimatedAnnualIncome(estimatedAnnualIncome)
                .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> getAggregatedStocks() {
        // Get open positions: ISIN aggregated across all depots
        List<Tuple> positions = em.createQuery("""
            SELECT t.isin.id as isinId, t.isin.isin as isinCode,
                   SUM(t.count) as totalCount,
                   SUM(t.count * t.sharePrice) as totalCost
            FROM TransactionEntity t
            GROUP BY t.isin.id, t.isin.isin
            HAVING SUM(t.count) > 0
            """, Tuple.class).getResultList();

        List<StockDto> result = new ArrayList<>();
        for (Tuple pos : positions) {
            Integer isinId = pos.get("isinId", Integer.class);
            String isinCode = pos.get("isinCode", String.class);
            Double totalCount = pos.get("totalCount", Double.class);
            Double totalCost = pos.get("totalCost", Double.class);

            double avgEntryPrice = totalCount > 0 ? totalCost / totalCount : 0;

            String name = getFirstName(isinId);
            String country = getCountry(isinId);
            String branch = getBranch(isinId);
            Double currentQuote = getQuote(isinId);
            Double dps = getDividendPerShare(isinId);

            Double performancePercent = null;
            if (currentQuote != null && avgEntryPrice > 0) {
                performancePercent = (currentQuote - avgEntryPrice) / avgEntryPrice * 100;
            }

            Double estimatedAnnualIncome = (dps != null && dps > 0) ? dps * totalCount : null;

            result.add(StockDto.builder()
                .isin(isinCode).name(name).country(country).branch(branch)
                .count(totalCount).avgEntryPrice(avgEntryPrice)
                .currentQuote(currentQuote).performancePercent(performancePercent)
                .dividendPerShare(dps).estimatedAnnualIncome(estimatedAnnualIncome)
                .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        List<StockDto> stocks = getStocks();

        double totalValue = stocks.stream()
            .mapToDouble(s -> s.getCount() * s.getAvgEntryPrice())
            .sum();

        int stockCount = (int) stocks.stream().map(StockDto::getIsin).distinct().count();

        double totalDividendIncome = stocks.stream()
            .filter(s -> s.getEstimatedAnnualIncome() != null)
            .mapToDouble(StockDto::getEstimatedAnnualIncome)
            .sum();

        double dividendRatio = totalValue > 0 ? (totalDividendIncome / totalValue) * 100 : 0;

        List<HoldingDto> top5Holdings = stocks.stream()
            .sorted(Comparator.comparingDouble((StockDto s) -> s.getCount() * s.getAvgEntryPrice()).reversed())
            .limit(5)
            .map(s -> HoldingDto.builder()
                .isin(s.getIsin()).name(s.getName())
                .investedAmount(s.getCount() * s.getAvgEntryPrice())
                .build())
            .toList();

        List<DividendSourceDto> top5Dividends = stocks.stream()
            .filter(s -> s.getEstimatedAnnualIncome() != null && s.getEstimatedAnnualIncome() > 0)
            .sorted(Comparator.comparingDouble(StockDto::getEstimatedAnnualIncome).reversed())
            .limit(5)
            .map(s -> DividendSourceDto.builder()
                .isin(s.getIsin()).name(s.getName())
                .estimatedAnnualIncome(s.getEstimatedAnnualIncome())
                .build())
            .toList();

        LocalDateTime lastFetch = settingRepo.findByKey("quote.last.fetch.timestamp")
            .map(s -> LocalDateTime.parse(s.getValue()))
            .orElse(null);

        return DashboardDto.builder()
            .totalPortfolioValue(totalValue)
            .stockCount(stockCount)
            .totalDividendRatio(dividendRatio)
            .top5Holdings(top5Holdings)
            .top5DividendSources(top5Dividends)
            .lastQuoteFetchAt(lastFetch)
            .build();
    }

    @Transactional(readOnly = true)
    public DiversificationDto getCountryDiversification() {
        return getDiversification("isin_country", "country", "country_id");
    }

    @Transactional(readOnly = true)
    public DiversificationDto getBranchDiversification() {
        return getDiversification("isin_branch", "branch", "branch_id");
    }

    @SuppressWarnings("unchecked")
    private DiversificationDto getDiversification(String joinTable, String refTable, String fkColumn) {
        String sql = "SELECT r.name, SUM(pos.invested) as invested"
            + " FROM ("
            + "     SELECT t.isin_id,"
            + "            SUM(t.count * t.share_price) as invested"
            + "     FROM transaction t"
            + "     GROUP BY t.isin_id"
            + "     HAVING SUM(t.count) > 0"
            + " ) pos"
            + " JOIN " + joinTable + " jt ON jt.isin_id = pos.isin_id"
            + " JOIN " + refTable + " r ON r.id = jt." + fkColumn
            + " GROUP BY r.name"
            + " ORDER BY invested DESC";

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        double total = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).sum();

        List<DiversificationEntry> entries = rows.stream()
            .map(r -> DiversificationEntry.builder()
                .name((String) r[0])
                .investedAmount(((Number) r[1]).doubleValue())
                .percentage(total > 0 ? ((Number) r[1]).doubleValue() / total * 100 : 0)
                .build())
            .toList();

        return DiversificationDto.builder().entries(entries).totalInvested(total).build();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TransactionDto> getTransactions(TransactionFilter filter) {
        StringBuilder jpql = new StringBuilder(
            "SELECT t FROM TransactionEntity t JOIN FETCH t.isin JOIN FETCH t.depot WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.isin() != null && !filter.isin().isBlank()) {
            jpql.append(" AND LOWER(t.isin.isin) LIKE :isin");
            params.put("isin", "%" + filter.isin().toLowerCase() + "%");
        }
        if (filter.depot() != null && !filter.depot().isBlank()) {
            List<String> depotValues = java.util.Arrays.stream(filter.depot().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (depotValues.size() == 1) {
                jpql.append(" AND t.depot.name = :depot");
                params.put("depot", depotValues.get(0));
            } else if (!depotValues.isEmpty()) {
                jpql.append(" AND t.depot.name IN :depots");
                params.put("depots", depotValues);
            }
        }
        if (filter.fromDate() != null) {
            jpql.append(" AND t.date >= :fromDate");
            params.put("fromDate", filter.fromDate());
        }
        if (filter.toDate() != null) {
            jpql.append(" AND t.date <= :toDate");
            params.put("toDate", filter.toDate());
        }
        jpql.append(" ORDER BY t.date DESC");

        var query = em.createQuery(jpql.toString(), com.folio.model.TransactionEntity.class);
        params.forEach(query::setParameter);
        List<com.folio.model.TransactionEntity> txns = query.getResultList();

        List<TransactionDto> result = txns.stream().map(t -> TransactionDto.builder()
            .id(t.getId())
            .date(t.getDate())
            .isin(t.getIsin().getIsin())
            .name(getFirstName(t.getIsin().getId()))
            .depot(t.getDepot().getName())
            .count(t.getCount())
            .sharePrice(t.getSharePrice())
            .build())
            .toList();

        // Apply name filter in-memory (name comes from a separate lookup).
        // Filter by matching ISIN codes so that all transactions (buy + sell) for a
        // matching stock are included, even if some transactions lack a resolved name.
        if (filter.name() != null && !filter.name().isBlank()) {
            String lower = filter.name().toLowerCase();
            Set<String> matchingIsins = result.stream()
                .filter(t -> t.getName() != null && t.getName().toLowerCase().contains(lower))
                .map(TransactionDto::getIsin)
                .collect(Collectors.toSet());
            result = result.stream()
                .filter(t -> matchingIsins.contains(t.getIsin()))
                .toList();
        }

        return result;
    }

    private String getFirstName(Integer isinId) {
        try {
            return (String) em.createQuery("SELECT n.name FROM IsinNameEntity n WHERE n.isin.id = :id ORDER BY n.id ASC")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private String getCountry(Integer isinId) {
        try {
            return (String) em.createNativeQuery(
                "SELECT c.name FROM isin_country ic JOIN country c ON c.id = ic.country_id WHERE ic.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private String getBranch(Integer isinId) {
        try {
            return (String) em.createNativeQuery(
                "SELECT b.name FROM isin_branch ib JOIN branch b ON b.id = ib.branch_id WHERE ib.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getQuote(Integer isinId) {
        try {
            return (Double) em.createQuery("SELECT q.value FROM IsinQuoteEntity q WHERE q.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDividendPerShare(Integer isinId) {
        try {
            return (Double) em.createQuery("SELECT d.dividendPerShare FROM DividendEntity d WHERE d.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}