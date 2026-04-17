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
import com.folio.model.TransactionEntity;
import com.folio.repository.SettingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class PortfolioService {

    private static final int TOP_N = 5;
    private static final int PERCENT = 100;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final EntityManager entityManager;
    private final SettingRepository settingRepository;

    PortfolioService(EntityManager entityManager, SettingRepository settingRepository) {
        this.entityManager = requireNonNull(entityManager);
        this.settingRepository = requireNonNull(settingRepository);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> stocks() {
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
            result.add(toStockDto(position, position.get("depotName", String.class)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StockDto> aggregatedStocks() {
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
            result.add(toStockDto(position, null));
        }
        return result;
    }

    private StockDto toStockDto(Tuple position, String depotName) {
        Integer isinId = position.get("isinId", Integer.class);
        Isin isin = new Isin(position.get("isin", String.class));
        Double totalCount = position.get("totalCount", Double.class);
        Double totalCost = position.get("totalCost", Double.class);

        double avgEntryPrice = totalCount > 0 ? totalCost / totalCount : 0;

        String name = findFirstName(isinId);
        Double currentQuote = findQuote(isinId);
        Double dividendPerShare = findDividendPerShare(isinId);
        Double performancePercent = calculatePerformancePercent(currentQuote, avgEntryPrice);
        Double estimatedAnnualIncome = calculateEstimatedAnnualIncome(dividendPerShare, totalCount);

        return new StockDto(
            new SecurityIdentity(isin, findTickerSymbol(isinId), name),
            new StockClassification(findCountry(isinId), findBranch(isinId), depotName),
            new StockMetrics(
                new StockPosition(totalCount, avgEntryPrice, currentQuote),
                new StockPerformance(performancePercent, dividendPerShare, estimatedAnnualIncome)));
    }

    private Double calculatePerformancePercent(Double currentQuote, double avgEntryPrice) {
        if (currentQuote == null || avgEntryPrice <= 0) {
            return null;
        }
        return (currentQuote - avgEntryPrice) / avgEntryPrice * PERCENT;
    }

    private Double calculateEstimatedAnnualIncome(Double dividendPerShare, Double totalCount) {
        if (dividendPerShare == null || dividendPerShare <= 0) {
            return null;
        }
        return dividendPerShare * totalCount;
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard() {
        List<StockDto> allStocks = stocks();

        double totalValue = totalInvestedValue(allStocks);
        int stockCount = (int) allStocks.stream().map(stock -> stock.security().isin()).distinct().count();
        double totalDividendIncome = totalDividendIncome(allStocks);
        double dividendRatio = totalValue > 0 ? (totalDividendIncome / totalValue) * PERCENT : 0;

        List<HoldingDto> top5Holdings = topHoldings(allStocks);
        List<DividendSourceDto> top5Dividends = topDividends(allStocks);
        String lastFetchFormatted = formattedLastFetchTimestamp();

        return new DashboardDto(
            new DashboardSummary(totalValue, stockCount, dividendRatio),
            new DashboardLists(top5Holdings, top5Dividends),
            lastFetchFormatted);
    }

    private double totalInvestedValue(List<StockDto> allStocks) {
        return allStocks.stream()
            .mapToDouble(stock -> stock.metrics().position().count() * stock.metrics().position().avgEntryPrice())
            .sum();
    }

    private double totalDividendIncome(List<StockDto> allStocks) {
        return allStocks.stream()
            .filter(stock -> stock.metrics().performance().estimatedAnnualIncome() != null)
            .mapToDouble(stock -> stock.metrics().performance().estimatedAnnualIncome())
            .sum();
    }

    private List<HoldingDto> topHoldings(List<StockDto> allStocks) {
        return allStocks.stream()
            .sorted(Comparator.comparingDouble((StockDto stock) -> stock.metrics().position().count() * stock.metrics().position().avgEntryPrice()).reversed())
            .limit(TOP_N)
            .map(stock -> new HoldingDto(stock.security().isin(), stock.security().name(), stock.metrics().position().count() * stock.metrics().position().avgEntryPrice()))
            .toList();
    }

    private List<DividendSourceDto> topDividends(List<StockDto> allStocks) {
        return allStocks.stream()
            .filter(this::hasEstimatedAnnualIncome)
            .sorted(Comparator.comparingDouble((StockDto stock) -> stock.metrics().performance().estimatedAnnualIncome()).reversed())
            .limit(TOP_N)
            .map(stock -> new DividendSourceDto(stock.security().isin(), stock.security().name() != null ? stock.security().name() : stock.security().isin().value(), stock.metrics().performance().estimatedAnnualIncome()))
            .toList();
    }

    private boolean hasEstimatedAnnualIncome(StockDto stock) {
        return stock.metrics().performance().estimatedAnnualIncome() != null
            && stock.metrics().performance().estimatedAnnualIncome() > 0;
    }

    private String formattedLastFetchTimestamp() {
        LocalDateTime lastFetch = settingRepository.findByKey("quote.last.fetch.timestamp")
            .map(setting -> LocalDateTime.parse(setting.getValue()))
            .orElse(null);
        if (lastFetch == null) {
            return null;
        }
        return lastFetch.format(DATE_FORMAT);
    }

    @Transactional(readOnly = true)
    public DiversificationDto countryDiversification() {
        return buildDiversification(new DiversificationQuery("isin_country", "country", "country_id"));
    }

    @Transactional(readOnly = true)
    public DiversificationDto branchDiversification() {
        return buildDiversification(new DiversificationQuery("isin_branch", "branch", "branch_id"));
    }

    @SuppressWarnings("unchecked")
    private DiversificationDto buildDiversification(DiversificationQuery query) {
        String sql = "SELECT r.name, SUM(pos.invested) as invested"
            + " FROM ("
            + "     SELECT t.isin_id,"
            + "            SUM(t.count * t.share_price) as invested"
            + "     FROM transaction t"
            + "     GROUP BY t.isin_id"
            + "     HAVING SUM(t.count) > 0"
            + " ) pos"
            + " JOIN " + query.joinTable() + " jt ON jt.isin_id = pos.isin_id"
            + " JOIN " + query.refTable() + " r ON r.id = jt." + query.fkColumn()
            + " GROUP BY r.name"
            + " ORDER BY invested DESC";

        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

        double total = rows.stream().mapToDouble(row -> ((Number) row[1]).doubleValue()).sum();

        List<DiversificationEntry> entries = rows.stream()
            .map(row -> new DiversificationEntry(
                (String) row[0],
                ((Number) row[1]).doubleValue(),
                total > 0 ? ((Number) row[1]).doubleValue() / total * PERCENT : 0))
            .toList();

        return new DiversificationDto(entries, total);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TransactionDto> findTransactions(TransactionFilter filter) {
        StringBuilder jpql = new StringBuilder(
            "SELECT t FROM TransactionEntity t JOIN FETCH t.context.isin JOIN FETCH t.context.depot WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        appendIsinFilter(jpql, params, filter);
        appendDepotFilter(jpql, params, filter);
        appendDateFilters(jpql, params, filter);
        jpql.append(" ORDER BY t.context.date DESC");

        var query = entityManager.createQuery(jpql.toString(), TransactionEntity.class);
        params.forEach(query::setParameter);
        List<TransactionEntity> allTransactions = query.getResultList();

        List<TransactionDto> result = allTransactions.stream().map(this::toTransactionDto).toList();
        result = filterByName(result, filter);
        result = filterByTickerSymbol(result, filter);
        return result;
    }

    private TransactionDto toTransactionDto(TransactionEntity transaction) {
        String formattedDate = transaction.getDate().format(TRANSACTION_DATE_FORMAT);
        return new TransactionDto(
            new TransactionIdentity(transaction.getId(), formattedDate, transaction.getDate()),
            new SecurityIdentity(new Isin(transaction.getIsin().getIsin()), findTickerSymbol(transaction.getIsin().getId()), findFirstName(transaction.getIsin().getId())),
            new TransactionTradeData(transaction.getDepot().getName(), transaction.getCount(), transaction.getSharePrice()));
    }

    private void appendIsinFilter(StringBuilder jpql, Map<String, Object> params, TransactionFilter filter) {
        if (filter.isinFragment() != null && !filter.isinFragment().isBlank()) {
            jpql.append(" AND LOWER(t.context.isin.isin) LIKE :isin");
            params.put("isin", "%" + filter.isinFragment().toLowerCase() + "%");
        }
    }

    private void appendDepotFilter(StringBuilder jpql, Map<String, Object> params, TransactionFilter filter) {
        if (filter.depotFragment() == null || filter.depotFragment().isBlank()) {
            return;
        }
        List<String> depotValues = Arrays.stream(filter.depotFragment().split(","))
            .map(String::trim).filter(value -> !value.isEmpty()).toList();
        if (depotValues.size() == 1) {
            jpql.append(" AND t.context.depot.name = :depot");
            params.put("depot", depotValues.get(0));
        } else if (!depotValues.isEmpty()) {
            jpql.append(" AND t.context.depot.name IN :depots");
            params.put("depots", depotValues);
        }
    }

    private void appendDateFilters(StringBuilder jpql, Map<String, Object> params, TransactionFilter filter) {
        if (filter.fromDate() != null) {
            jpql.append(" AND t.context.date >= :fromDate");
            params.put("fromDate", filter.fromDate());
        }
        if (filter.toDate() != null) {
            jpql.append(" AND t.context.date <= :toDate");
            params.put("toDate", filter.toDate());
        }
    }

    private List<TransactionDto> filterByName(List<TransactionDto> result, TransactionFilter filter) {
        if (filter.nameFragment() == null || filter.nameFragment().isBlank()) {
            return result;
        }
        String lower = filter.nameFragment().toLowerCase();
        Set<Isin> matchingIsins = result.stream()
            .filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(lower))
            .map(TransactionDto::getIsin)
            .collect(Collectors.toSet());
        return result.stream()
            .filter(dto -> matchingIsins.contains(dto.getIsin()))
            .toList();
    }

    private List<TransactionDto> filterByTickerSymbol(List<TransactionDto> result, TransactionFilter filter) {
        if (filter.tickerSymbolFragment() == null || filter.tickerSymbolFragment().isBlank()) {
            return result;
        }
        String lower = filter.tickerSymbolFragment().toLowerCase();
        Set<Isin> matchingIsins = result.stream()
            .filter(dto -> dto.getTickerSymbol() != null && dto.getTickerSymbol().toLowerCase().contains(lower))
            .map(TransactionDto::getIsin)
            .collect(Collectors.toSet());
        return result.stream()
            .filter(dto -> matchingIsins.contains(dto.getIsin()))
            .toList();
    }


    @SuppressWarnings("unchecked")
    private TickerSymbol findTickerSymbol(Integer isinId) {
        List<String> results = entityManager.createNativeQuery("""
                SELECT ts.symbol FROM isin_ticker it
                JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
                WHERE it.isin_id = :id
                """)
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return TickerSymbolFactory.of(results.get(0)).orElse(null);
    }

    private String findFirstName(Integer isinId) {
        List<String> results = entityManager.createQuery(
                "SELECT n.name FROM IsinNameEntity n WHERE n.isin.id = :id ORDER BY n.id ASC", String.class)
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @SuppressWarnings("unchecked")
    private String findCountry(Integer isinId) {
        List<String> results = entityManager.createNativeQuery(
                "SELECT c.name FROM isin_country ic JOIN country c ON c.id = ic.country_id WHERE ic.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @SuppressWarnings("unchecked")
    private String findBranch(Integer isinId) {
        List<String> results = entityManager.createNativeQuery(
                "SELECT b.name FROM isin_branch ib JOIN branch b ON b.id = ib.branch_id WHERE ib.isin_id = :id")
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Double findQuote(Integer isinId) {
        List<Double> results = entityManager.createQuery(
                "SELECT q.data.value FROM IsinQuoteEntity q WHERE q.source.isin.id = :id", Double.class)
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private Double findDividendPerShare(Integer isinId) {
        List<Double> results = entityManager.createQuery(
                "SELECT d.dividendPerShare FROM DividendEntity d WHERE d.reference.isin.id = :id", Double.class)
                .setParameter("id", isinId).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}

