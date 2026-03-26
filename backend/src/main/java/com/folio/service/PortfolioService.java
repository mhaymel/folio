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
        // Get open positions: ISIN with SUM(count) > 0
        List<Tuple> positions = em.createQuery("""
            SELECT t.isin.id as isinId, t.isin.isin as isinCode,
                   SUM(t.count) as totalShares,
                   SUM(t.count * t.sharePrice) as totalCost
            FROM Transaction t
            GROUP BY t.isin.id, t.isin.isin
            HAVING SUM(t.count) > 0
            """, Tuple.class).getResultList();

        List<StockDto> result = new ArrayList<>();
        for (Tuple pos : positions) {
            Integer isinId = pos.get("isinId", Integer.class);
            String isinCode = pos.get("isinCode", String.class);
            Double totalShares = pos.get("totalShares", Double.class);
            Double totalCost = pos.get("totalCost", Double.class);

            double avgEntryPrice = totalShares > 0 ? totalCost / totalShares : 0;

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

            Double estimatedAnnualIncome = (dps != null && dps > 0) ? dps * totalShares : null;

            result.add(StockDto.builder()
                .isin(isinCode).name(name).country(country).branch(branch)
                .totalShares(totalShares).avgEntryPrice(avgEntryPrice)
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
            .mapToDouble(s -> s.getTotalShares() * s.getAvgEntryPrice())
            .sum();

        int stockCount = stocks.size();

        double totalDividendIncome = stocks.stream()
            .filter(s -> s.getEstimatedAnnualIncome() != null)
            .mapToDouble(StockDto::getEstimatedAnnualIncome)
            .sum();

        double dividendRatio = totalValue > 0 ? (totalDividendIncome / totalValue) * 100 : 0;

        List<DashboardDto.HoldingDto> top5Holdings = stocks.stream()
            .sorted(Comparator.comparingDouble((StockDto s) -> s.getTotalShares() * s.getAvgEntryPrice()).reversed())
            .limit(5)
            .map(s -> DashboardDto.HoldingDto.builder()
                .isin(s.getIsin()).name(s.getName())
                .investedAmount(s.getTotalShares() * s.getAvgEntryPrice())
                .build())
            .toList();

        List<DashboardDto.DividendSourceDto> top5Dividends = stocks.stream()
            .filter(s -> s.getEstimatedAnnualIncome() != null && s.getEstimatedAnnualIncome() > 0)
            .sorted(Comparator.comparingDouble(StockDto::getEstimatedAnnualIncome).reversed())
            .limit(5)
            .map(s -> DashboardDto.DividendSourceDto.builder()
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
        List<Object[]> rows = em.createNativeQuery("""
            SELECT r.name, SUM(pos.invested) as invested
            FROM (
                SELECT t.isin_id,
                       SUM(t.count * t.share_price) as invested
                FROM transaction t
                GROUP BY t.isin_id
                HAVING SUM(t.count) > 0
            ) pos
            JOIN """ + joinTable + " jt ON jt.isin_id = pos.isin_id " + """
            JOIN """ + refTable + " r ON r.id = jt." + fkColumn + """

            GROUP BY r.name
            ORDER BY invested DESC
            """).getResultList();

        double total = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).sum();

        List<DiversificationDto.Entry> entries = rows.stream()
            .map(r -> DiversificationDto.Entry.builder()
                .name((String) r[0])
                .investedAmount(((Number) r[1]).doubleValue())
                .percentage(total > 0 ? ((Number) r[1]).doubleValue() / total * 100 : 0)
                .build())
            .toList();

        return DiversificationDto.builder().entries(entries).totalInvested(total).build();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TransactionDto> getTransactions(String isinFilter, String depotFilter,
                                                 LocalDateTime fromDate, LocalDateTime toDate) {
        StringBuilder jpql = new StringBuilder(
            "SELECT t FROM Transaction t JOIN FETCH t.isin JOIN FETCH t.depot WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (isinFilter != null && !isinFilter.isBlank()) {
            jpql.append(" AND t.isin.isin = :isin");
            params.put("isin", isinFilter);
        }
        if (depotFilter != null && !depotFilter.isBlank()) {
            jpql.append(" AND t.depot.name = :depot");
            params.put("depot", depotFilter);
        }
        if (fromDate != null) {
            jpql.append(" AND t.date >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            jpql.append(" AND t.date <= :toDate");
            params.put("toDate", toDate);
        }
        jpql.append(" ORDER BY t.date DESC");

        var query = em.createQuery(jpql.toString(), com.folio.model.Transaction.class);
        params.forEach(query::setParameter);
        List<com.folio.model.Transaction> txns = query.getResultList();

        return txns.stream().map(t -> TransactionDto.builder()
            .id(t.getId())
            .date(t.getDate())
            .isin(t.getIsin().getIsin())
            .name(getFirstName(t.getIsin().getId()))
            .depot(t.getDepot().getName())
            .count(t.getCount())
            .sharePrice(t.getSharePrice())
            .build())
            .toList();
    }

    private String getFirstName(Integer isinId) {
        try {
            return (String) em.createQuery("SELECT n.name FROM IsinName n WHERE n.isin.id = :id ORDER BY n.id ASC")
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
            return (Double) em.createQuery("SELECT q.value FROM IsinQuote q WHERE q.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDividendPerShare(Integer isinId) {
        try {
            return (Double) em.createQuery("SELECT d.dividendPerShare FROM Dividend d WHERE d.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}