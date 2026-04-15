package com.folio.service;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;
import com.folio.domain.TickerSymbolFactory;
import com.folio.dto.DashboardDto;
import com.folio.dto.DashboardLists;
import com.folio.dto.DashboardSummary;
import com.folio.dto.DiversificationDto;
import com.folio.dto.DiversificationEntry;
import com.folio.dto.DividendSourceDto;
import com.folio.dto.HoldingDto;
import com.folio.dto.SecurityIdentity;
import com.folio.dto.StockClassification;
import com.folio.dto.StockDto;
import com.folio.dto.StockMetrics;
import com.folio.dto.StockPerformance;
import com.folio.dto.StockPosition;
import com.folio.dto.TransactionDto;
import com.folio.dto.TransactionFilter;
import com.folio.dto.TransactionIdentity;
import com.folio.dto.TransactionTradeData;
import com.folio.repository.SettingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class PortfolioService {

    private final EntityManager entityManager;
    private final SettingRepository settingRepository;

    public PortfolioService(EntityManager entityManager, SettingRepository settingRepository) {
        this.entityManager = requireNonNull(entityManager);
        this.settingRepository = requireNonNull(settingRepository);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> getStocks() {
        List<Tuple> positions = entityManager.createQuery("""
            SELECT t.context.isin.id as isinId, t.context.isin.isin as isin,
                   t.context.depot.name as depotName,
                   SUM(t.values.count) as totalCount,
                   SUM(t.values.count * t.values.sharePrice) as totalCost
            FROM TransactionEntity t
            GROUP BY t.context.isin.id, t.context.isin.isin, t.context.depot.name
            HAVING SUM(t.values.count) > 0
            """, Tuple.class).getResultList();

        List<StockDto> result = new ArrayList<>();
        for (Tuple position : positions) {
            Integer isinId = position.get("isinId", Integer.class);
            Isin isin = new Isin(position.get("isin", String.class));
            String depotName = position.get("depotName", String.class);
            Double totalCount = position.get("totalCount", Double.class);
            Double totalCost = position.get("totalCost", Double.class);

            double avgEntryPrice = totalCount > 0 ? totalCost / totalCount : 0;

            String name = getFirstName(isinId);
            String country = getCountry(isinId);
            String branch = getBranch(isinId);
            Double currentQuote = getQuote(isinId);
            Double dividendPerShare = getDividendPerShare(isinId);

            Double performancePercent = null;
            if (currentQuote != null && avgEntryPrice > 0) {
                performancePercent = (currentQuote - avgEntryPrice) / avgEntryPrice * 100;
            }

            Double estimatedAnnualIncome = (dividendPerShare != null && dividendPerShare > 0) ? dividendPerShare * totalCount : null;

            result.add(new StockDto(
                new SecurityIdentity(isin, getTickerSymbol(isinId), name),
                new StockClassification(country, branch, depotName),
                new StockMetrics(
                    new StockPosition(totalCount, avgEntryPrice, currentQuote),
                    new StockPerformance(performancePercent, dividendPerShare, estimatedAnnualIncome))));
        }
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> getAggregatedStocks() {
        List<Tuple> positions = entityManager.createQuery("""
            SELECT t.context.isin.id as isinId, t.context.isin.isin as isin,
                   SUM(t.values.count) as totalCount,
                   SUM(t.values.count * t.values.sharePrice) as totalCost
            FROM TransactionEntity t
            GROUP BY t.context.isin.id, t.context.isin.isin
            HAVING SUM(t.values.count) > 0
            """, Tuple.class).getResultList();

        List<StockDto> result = new ArrayList<>();
        for (Tuple position : positions) {
            Integer isinId = position.get("isinId", Integer.class);
            Isin isin = new Isin(position.get("isin", String.class));
            Double totalCount = position.get("totalCount", Double.class);
            Double totalCost = position.get("totalCost", Double.class);

            double avgEntryPrice = totalCount > 0 ? totalCost / totalCount : 0;

            String name = getFirstName(isinId);
            String country = getCountry(isinId);
            String branch = getBranch(isinId);
            Double currentQuote = getQuote(isinId);
            Double dividendPerShare = getDividendPerShare(isinId);

            Double performancePercent = null;
            if (currentQuote != null && avgEntryPrice > 0) {
                performancePercent = (currentQuote - avgEntryPrice) / avgEntryPrice * 100;
            }

            Double estimatedAnnualIncome = (dividendPerShare != null && dividendPerShare > 0) ? dividendPerShare * totalCount : null;

            result.add(new StockDto(
                new SecurityIdentity(isin, getTickerSymbol(isinId), name),
                new StockClassification(country, branch, null),
                new StockMetrics(
                    new StockPosition(totalCount, avgEntryPrice, currentQuote),
                    new StockPerformance(performancePercent, dividendPerShare, estimatedAnnualIncome))));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        List<StockDto> stocks = getStocks();

        double totalValue = stocks.stream()
            .mapToDouble(stock -> stock.metrics().position().count() * stock.metrics().position().avgEntryPrice())
            .sum();

        int stockCount = (int) stocks.stream().map(stock -> stock.security().isin()).distinct().count();

        double totalDividendIncome = stocks.stream()
            .filter(stock -> stock.metrics().performance().estimatedAnnualIncome() != null)
            .mapToDouble(stock -> stock.metrics().performance().estimatedAnnualIncome())
            .sum();

        double dividendRatio = totalValue > 0 ? (totalDividendIncome / totalValue) * 100 : 0;

        List<HoldingDto> top5Holdings = stocks.stream()
            .sorted(Comparator.comparingDouble((StockDto stock) -> stock.metrics().position().count() * stock.metrics().position().avgEntryPrice()).reversed())
            .limit(5)
            .map(stock -> new HoldingDto(stock.security().isin(), stock.security().name(), stock.metrics().position().count() * stock.metrics().position().avgEntryPrice()))
            .toList();

        List<DividendSourceDto> top5Dividends = stocks.stream()
            .filter(stock -> stock.metrics().performance().estimatedAnnualIncome() != null && stock.metrics().performance().estimatedAnnualIncome() > 0)
            .sorted(Comparator.comparingDouble((StockDto stock) -> stock.metrics().performance().estimatedAnnualIncome()).reversed())
            .limit(5)
            .map(stock -> new DividendSourceDto(stock.security().isin(), stock.security().name() != null ? stock.security().name() : stock.security().isin().value(), stock.metrics().performance().estimatedAnnualIncome()))
            .toList();

        LocalDateTime lastFetch = settingRepository.findByKey("quote.last.fetch.timestamp")
            .map(setting -> LocalDateTime.parse(setting.getValue()))
            .orElse(null);

        String lastFetchFormatted = lastFetch != null
            ? lastFetch.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            : null;

        return new DashboardDto(
            new DashboardSummary(totalValue, stockCount, dividendRatio),
            new DashboardLists(top5Holdings, top5Dividends),
            lastFetchFormatted);
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

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

        double total = rows.stream().mapToDouble(row -> ((Number) row[1]).doubleValue()).sum();

        List<DiversificationEntry> entries = rows.stream()
            .map(row -> new DiversificationEntry(
                (String) row[0],
                ((Number) row[1]).doubleValue(),
                total > 0 ? ((Number) row[1]).doubleValue() / total * 100 : 0))
            .toList();

        return new DiversificationDto(entries, total);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TransactionDto> getTransactions(TransactionFilter filter) {
        StringBuilder jpql = new StringBuilder(
            "SELECT t FROM TransactionEntity t JOIN FETCH t.context.isin JOIN FETCH t.context.depot WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.isinFragment() != null && !filter.isinFragment().isBlank()) {
            jpql.append(" AND LOWER(t.context.isin.isin) LIKE :isin");
            params.put("isin", "%" + filter.isinFragment().toLowerCase() + "%");
        }
        if (filter.depotFragment() != null && !filter.depotFragment().isBlank()) {
            List<String> depotValues = java.util.Arrays.stream(filter.depotFragment().split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
            if (depotValues.size() == 1) {
                jpql.append(" AND t.context.depot.name = :depot");
                params.put("depot", depotValues.get(0));
            } else if (!depotValues.isEmpty()) {
                jpql.append(" AND t.context.depot.name IN :depots");
                params.put("depots", depotValues);
            }
        }
        if (filter.fromDate() != null) {
            jpql.append(" AND t.context.date >= :fromDate");
            params.put("fromDate", filter.fromDate());
        }
        if (filter.toDate() != null) {
            jpql.append(" AND t.context.date <= :toDate");
            params.put("toDate", filter.toDate());
        }
        jpql.append(" ORDER BY t.context.date DESC");

        var query = entityManager.createQuery(jpql.toString(), com.folio.model.TransactionEntity.class);
        params.forEach(query::setParameter);
        List<com.folio.model.TransactionEntity> allTransactions = query.getResultList();

        List<TransactionDto> result = allTransactions.stream().map(transaction -> {
            String formattedDate = transaction.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return new TransactionDto(
                new TransactionIdentity(transaction.getId(), formattedDate, transaction.getDate()),
                new SecurityIdentity(new Isin(transaction.getIsin().getIsin()), getTickerSymbol(transaction.getIsin().getId()), getFirstName(transaction.getIsin().getId())),
                new TransactionTradeData(transaction.getDepot().getName(), transaction.getCount(), transaction.getSharePrice()));
        }).toList();

        // Filter by matching ISIN codes so that all transactions (buy + sell) for a
        // matching stock are included, even if some transactions lack a resolved name.
        if (filter.nameFragment() != null && !filter.nameFragment().isBlank()) {
            String lower = filter.nameFragment().toLowerCase();
            Set<Isin> matchingIsins = result.stream()
                .filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(lower))
                .map(TransactionDto::getIsin)
                .collect(Collectors.toSet());
            result = result.stream()
                .filter(dto -> matchingIsins.contains(dto.getIsin()))
                .toList();
        }

        if (filter.tickerSymbolFragment() != null && !filter.tickerSymbolFragment().isBlank()) {
            String lower = filter.tickerSymbolFragment().toLowerCase();
            Set<Isin> matchingIsins = result.stream()
                .filter(dto -> dto.getTickerSymbol() != null && dto.getTickerSymbol().toLowerCase().contains(lower))
                .map(TransactionDto::getIsin)
                .collect(Collectors.toSet());
            result = result.stream()
                .filter(dto -> matchingIsins.contains(dto.getIsin()))
                .toList();
        }

        return result;
    }

    private TickerSymbol getTickerSymbol(Integer isinId) {
        try {
            String symbol = (String) entityManager.createNativeQuery("""
                SELECT ts.symbol FROM isin_ticker it
                JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
                WHERE it.isin_id = :id
                """)
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
            return TickerSymbolFactory.of(symbol).orElse(null);
        } catch (Exception exception) {
            return null;
        }
    }

    private String getFirstName(Integer isinId) {
        try {
            return (String) entityManager.createQuery("SELECT n.name FROM IsinNameEntity n WHERE n.isin.id = :id ORDER BY n.id ASC")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }

    private String getCountry(Integer isinId) {
        try {
            return (String) entityManager.createNativeQuery(
                "SELECT c.name FROM isin_country ic JOIN country c ON c.id = ic.country_id WHERE ic.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }

    private String getBranch(Integer isinId) {
        try {
            return (String) entityManager.createNativeQuery(
                "SELECT b.name FROM isin_branch ib JOIN branch b ON b.id = ib.branch_id WHERE ib.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }

    private Double getQuote(Integer isinId) {
        try {
            return (Double) entityManager.createQuery("SELECT q.data.value FROM IsinQuoteEntity q WHERE q.source.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }

    private Double getDividendPerShare(Integer isinId) {
        try {
            return (Double) entityManager.createQuery("SELECT d.dividendPerShare FROM DividendEntity d WHERE d.reference.isin.id = :id")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }
}

