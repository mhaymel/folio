package com.folio.service;

import com.folio.domain.Isin;
import com.folio.dto.DashboardDto;
import com.folio.dto.DiversificationDto;
import com.folio.dto.StockDto;
import com.folio.model.*;
import com.folio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Integration tests for {@link PortfolioService} running against the H2 in-memory database.
 * <p>
 * Each test method is @Transactional so all data is rolled back after the test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PortfolioServiceTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private IsinRepository isinRepo;

    @Autowired
    private DepotRepository depotRepo;

    @Autowired
    private TransactionRepository txnRepo;

    @Autowired
    private IsinNameRepository isinNameRepo;

    @Autowired
    private DividendRepository dividendRepo;

    @Autowired
    private CurrencyRepository currencyRepo;

    @Autowired
    private IsinQuoteRepository isinQuoteRepo;

    @Autowired
    private QuoteProviderRepository quoteProviderRepo;

    @Autowired
    private SettingRepository settingRepo;

    // Shared test fixtures
    private DepotEntity depotDeGiro;
    private DepotEntity depotZero;

    @BeforeEach
    void setUp() {
        // The two depots already exist via Flyway seed data
        depotDeGiro = depotRepo.findByName("DeGiro").orElseThrow();
        depotZero = depotRepo.findByName("ZERO").orElseThrow();
    }

    // ------------------------------------------------------------------ helpers

    private IsinEntity createIsin(String code) {
        return isinRepo.findByIsin(code).orElseGet(() -> {
            IsinEntity isin = new IsinEntity();
            isin.setIsin(code);
            return isinRepo.save(isin);
        });
    }

    private void createIsinName(IsinEntity isin, String name) {
        if (isinNameRepo.existsByIsinIdAndName(isin.getId(), name)) {
            return;
        }
        IsinNameEntity isinName = new IsinNameEntity();
        isinName.setIsin(isin);
        isinName.setName(name);
        isinNameRepo.save(isinName);
    }

    private void createTransaction(IsinEntity isin, DepotEntity depot, double count, double sharePrice) {
        TransactionEntity t = new TransactionEntity(null,
                new com.folio.model.TransactionContext(LocalDateTime.of(2026, 1, 15, 10, 0), isin, depot),
                new com.folio.model.TransactionValues(count, sharePrice));
        txnRepo.save(t);
    }

    private void createDividend(IsinEntity isin, double dps) {
        CurrencyEntity eur = currencyRepo.findByName("EUR").orElseThrow();
        DividendEntity d = new DividendEntity();
        d.setIsin(isin);
        d.setCurrency(eur);
        d.setDividendPerShare(dps);
        dividendRepo.save(d);
    }

    private void createQuote(IsinEntity isin, double value) {
        QuoteProviderEntity provider = quoteProviderRepo.findByName("JustETF").orElseThrow();
        IsinQuoteEntity q = new IsinQuoteEntity();
        q.setIsin(isin);
        q.setQuoteProvider(provider);
        q.setValue(value);
        q.setFetchedAt(LocalDateTime.now());
        isinQuoteRepo.save(q);
    }

    // =========================================================================
    // getStocks()
    // =========================================================================

    @Test
    void getStocksEmptyPortfolioReturnsEmptyList() {
        assertThat(portfolioService.getStocks()).isEmpty();
    }

    @Test
    void getStocksSinglePositionInOneDepot() {
        IsinEntity basf = createIsin("DE000BASF111");
        createIsinName(basf, "BASF SE");
        createTransaction(basf, depotDeGiro, 10, 50.0);

        List<StockDto> stocks = portfolioService.getStocks();

        assertThat(stocks).hasSize(1);
        StockDto s = stocks.get(0);
        assertThat(s.security().isin()).isEqualTo(new Isin("DE000BASF111"));
        assertThat(s.security().name()).isEqualTo("BASF SE");
        assertThat(s.classification().depot()).isEqualTo("DeGiro");
        assertThat(s.metrics().position().count()).isEqualTo(10.0);
        assertThat(s.metrics().position().avgEntryPrice()).isEqualTo(50.0);
    }

    @Test
    void getStocksSameIsinInTwoDepotsReturnsTwoRows() {
        IsinEntity apple = createIsin("US0378331005");
        createIsinName(apple, "Apple Inc.");
        createTransaction(apple, depotDeGiro, 5, 150.0);
        createTransaction(apple, depotZero, 3, 160.0);

        List<StockDto> stocks = portfolioService.getStocks();

        assertThat(stocks).hasSize(2);
        assertThat(stocks).extracting(s -> s.classification().depot())
                .containsExactlyInAnyOrder("DeGiro", "ZERO");
        assertThat(stocks).allMatch(s -> new Isin("US0378331005").equals(s.security().isin()));
    }

    @Test
    void getStocksSoldPositionExcluded() {
        IsinEntity basf = createIsin("DE000BASF111");
        createTransaction(basf, depotDeGiro, 10, 50.0);   // buy
        createTransaction(basf, depotDeGiro, -10, 55.0);   // sell

        assertThat(portfolioService.getStocks()).isEmpty();
    }

    @Test
    void getStocksAvgEntryPriceCalculatedCorrectly() {
        IsinEntity isin = createIsin("IE00B1XNT000");
        createTransaction(isin, depotDeGiro, 10, 100.0);  // cost = 1000
        createTransaction(isin, depotDeGiro, 10, 200.0);  // cost = 2000

        StockDto s = portfolioService.getStocks().get(0);
        // total 20 shares, total cost 3000, avg = 150
        assertThat(s.metrics().position().count()).isEqualTo(20.0);
        assertThat(s.metrics().position().avgEntryPrice()).isCloseTo(150.0, within(0.001));
    }

    @Test
    void getStocksWithQuotePerformanceCalculated() {
        IsinEntity isin = createIsin("IE00B1XNT000");
        createTransaction(isin, depotDeGiro, 10, 100.0);
        createQuote(isin, 120.0);

        StockDto s = portfolioService.getStocks().get(0);
        assertThat(s.metrics().position().currentQuote()).isEqualTo(120.0);
        // performance = (120 - 100) / 100 * 100 = 20%
        assertThat(s.metrics().performance().performancePercent()).isCloseTo(20.0, within(0.001));
    }

    @Test
    void getStocksWithDividendEstimatedAnnualIncomeCalculated() {
        IsinEntity isin = createIsin("DE000BASF111");
        createTransaction(isin, depotDeGiro, 10, 50.0);
        createDividend(isin, 3.40);

        StockDto s = portfolioService.getStocks().get(0);
        assertThat(s.metrics().performance().dividendPerShare()).isEqualTo(3.40);
        assertThat(s.metrics().performance().estimatedAnnualIncome()).isCloseTo(34.0, within(0.001));
    }

    // =========================================================================
    // getAggregatedStocks()
    // =========================================================================

    @Test
    void getAggregatedStocksSameIsinInTwoDepotsReturnsOneRow() {
        IsinEntity apple = createIsin("US0378331005");
        createIsinName(apple, "Apple Inc.");
        createTransaction(apple, depotDeGiro, 5, 150.0);
        createTransaction(apple, depotZero, 3, 160.0);

        List<StockDto> stocks = portfolioService.getAggregatedStocks();

        assertThat(stocks).hasSize(1);
        StockDto s = stocks.get(0);
        assertThat(s.security().isin()).isEqualTo(new Isin("US0378331005"));
        assertThat(s.metrics().position().count()).isEqualTo(8.0);
        // total cost = 5*150 + 3*160 = 750 + 480 = 1230, avg = 1230 / 8 = 153.75
        assertThat(s.metrics().position().avgEntryPrice()).isCloseTo(153.75, within(0.001));
        // depot is null for aggregated stocks
        assertThat(s.classification().depot()).isNull();
    }

    // =========================================================================
    // getDashboard()
    // =========================================================================

    @Test
    void getDashboardEmptyPortfolioReturnsZeros() {
        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getStockCount()).isEqualTo(0);
        assertThat(d.getTotalPortfolioValue()).isEqualTo(0.0);
        assertThat(d.getTotalDividendRatio()).isEqualTo(0.0);
        assertThat(d.getTop5Holdings()).isEmpty();
        assertThat(d.getTop5DividendSources()).isEmpty();
        assertThat(d.getLastQuoteFetchAt()).isNull();
    }

    @Test
    void getDashboardSameStockInDifferentDepotsCountedOnce() {
        // This is the key requirement from the spec:
        //   "Stock count: number of distinct ISINs with SUM(count) > 0 across all
        //    depots (a product held in two depots counts as one)."

        IsinEntity apple = createIsin("US0378331005");
        createIsinName(apple, "Apple Inc.");
        createTransaction(apple, depotDeGiro, 5, 150.0);
        createTransaction(apple, depotZero, 3, 160.0);

        DashboardDto d = portfolioService.getDashboard();

        // Apple is in 2 depots but must be counted as 1 stock
        assertThat(d.getStockCount()).isEqualTo(1);
    }

    @Test
    void getDashboardTwoDistinctStocksInSameDepotCountedAsTwoStocks() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(apple, depotDeGiro, 5, 150.0);

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getStockCount()).isEqualTo(2);
    }

    @Test
    void getDashboardTwoDistinctStocksEachInTwoDepotsCountedAsTwoStocks() {
        // 2 distinct ISINs × 2 depots = 4 stock rows, but only 2 distinct ISINs
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(basf, depotZero, 5, 48.0);
        createTransaction(apple, depotDeGiro, 5, 150.0);
        createTransaction(apple, depotZero, 3, 160.0);

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getStockCount()).isEqualTo(2);
    }

    @Test
    void getDashboardSoldStockNotCounted() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(basf, depotDeGiro, -10, 55.0);  // fully sold
        createTransaction(apple, depotDeGiro, 5, 150.0);

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getStockCount()).isEqualTo(1);
    }

    @Test
    void getDashboardTotalPortfolioValue() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);    // invested 500
        createTransaction(apple, depotZero, 5, 150.0);      // invested 750

        DashboardDto d = portfolioService.getDashboard();

        // totalPortfolioValue = SUM(count * avgEntryPrice) = 500 + 750 = 1250
        assertThat(d.getTotalPortfolioValue()).isCloseTo(1250.0, within(0.001));
    }

    @Test
    void getDashboardDividendRatio() {
        IsinEntity basf = createIsin("DE000BASF111");
        createTransaction(basf, depotDeGiro, 10, 50.0);   // invested 500
        createDividend(basf, 3.40);                         // annual income = 34

        DashboardDto d = portfolioService.getDashboard();

        // ratio = (34 / 500) * 100 = 6.8%
        assertThat(d.getTotalDividendRatio()).isCloseTo(6.8, within(0.001));
    }

    @Test
    void getDashboardTop5HoldingsOrderedByInvestedAmount() {
        // Create 6 stocks with different invested amounts
        String[] isins = {"IE00B1XNT001", "IE00B1XNT002", "IE00B1XNT003",
                          "IE00B1XNT004", "IE00B1XNT005", "IE00B1XNT006"};
        double[] prices = {10, 20, 30, 40, 50, 60};

        for (int i = 0; i < 6; i++) {
            IsinEntity isin = createIsin(isins[i]);
            createIsinName(isin, "Stock " + (i + 1));
            createTransaction(isin, depotDeGiro, 10, prices[i]);
        }

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getTop5Holdings()).hasSize(5);
        // highest invested first: Stock6 (600), Stock5 (500), Stock4 (400), Stock3 (300), Stock2 (200)
        assertThat(d.getTop5Holdings().get(0).getIsin()).isEqualTo(new Isin("IE00B1XNT006"));
        assertThat(d.getTop5Holdings().get(0).getInvestedAmount()).isCloseTo(600.0, within(0.001));
        // Stock1 (100) should be excluded
        assertThat(d.getTop5Holdings()).extracting("isin")
                .doesNotContain("IE00B1XNT001");
    }

    @Test
    void getDashboardTop5DividendSources() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createIsinName(basf, "BASF SE");
        createIsinName(apple, "Apple Inc.");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(apple, depotDeGiro, 5, 150.0);
        createDividend(basf, 3.40);  // income = 34
        createDividend(apple, 0.96); // income = 4.8

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getTop5DividendSources()).hasSize(2);
        // BASF should be first (higher income)
        assertThat(d.getTop5DividendSources().get(0).isin()).isEqualTo(new Isin("DE000BASF111"));
        assertThat(d.getTop5DividendSources().get(0).estimatedAnnualIncome()).isCloseTo(34.0, within(0.001));
    }

    @Test
    void getDashboardLastQuoteFetchAtPresentWhenSettingExists() {
        SettingEntity s = new SettingEntity();
        s.setKey("quote.last.fetch.timestamp");
        s.setValue("2026-03-27T10:00:00");
        settingRepo.save(s);

        DashboardDto d = portfolioService.getDashboard();

        assertThat(d.getLastQuoteFetchAt()).isEqualTo("27.03.2026 10:00");
    }

    // =========================================================================
    // getDiversification (country / branch)
    // =========================================================================

    @Test
    void getCountryDiversificationEmptyPortfolio() {
        DiversificationDto d = portfolioService.getCountryDiversification();

        assertThat(d.getEntries()).isEmpty();
        assertThat(d.getTotalInvested()).isEqualTo(0.0);
    }

    @Test
    void getCountryDiversificationWithData() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);   // invested 500
        createTransaction(apple, depotDeGiro, 5, 100.0);  // invested 500

        // Insert country mappings via native SQL
        insertCountry(basf.getId(), "Germany");
        insertCountry(apple.getId(), "USA");

        DiversificationDto d = portfolioService.getCountryDiversification();

        assertThat(d.getTotalInvested()).isCloseTo(1000.0, within(0.001));
        assertThat(d.getEntries()).hasSize(2);
        assertThat(d.getEntries()).extracting("name")
                .containsExactlyInAnyOrder("Germany", "USA");
        // Each is 50%
        assertThat(d.getEntries().get(0).getPercentage()).isCloseTo(50.0, within(0.001));
    }

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private void insertCountry(Integer isinId, String countryName) {
        // Insert country if not exists, then link via isin_country (also if not exists)
        entityManager.createNativeQuery("INSERT INTO country (name) SELECT :name WHERE NOT EXISTS (SELECT 1 FROM country WHERE name = :name)")
                .setParameter("name", countryName).executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO isin_country (isin_id, country_id) SELECT :isinId, (SELECT id FROM country WHERE name = :name) WHERE NOT EXISTS (SELECT 1 FROM isin_country WHERE isin_id = :isinId)")
                .setParameter("isinId", isinId).setParameter("name", countryName).executeUpdate();
        entityManager.flush();
    }

    private void insertBranch(Integer isinId, String branchName) {
        entityManager.createNativeQuery("INSERT INTO branch (name) SELECT :name WHERE NOT EXISTS (SELECT 1 FROM branch WHERE name = :name)")
                .setParameter("name", branchName).executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO isin_branch (isin_id, branch_id) SELECT :isinId, (SELECT id FROM branch WHERE name = :name) WHERE NOT EXISTS (SELECT 1 FROM isin_branch WHERE isin_id = :isinId)")
                .setParameter("isinId", isinId).setParameter("name", branchName).executeUpdate();
        entityManager.flush();
    }

    @Test
    void getBranchDiversificationWithData() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createTransaction(basf, depotDeGiro, 10, 50.0);   // invested 500
        createTransaction(apple, depotDeGiro, 5, 200.0);  // invested 1000

        insertBranch(basf.getId(), "Chemicals");
        insertBranch(apple.getId(), "Technology");

        DiversificationDto d = portfolioService.getBranchDiversification();

        assertThat(d.getTotalInvested()).isCloseTo(1500.0, within(0.001));
        assertThat(d.getEntries()).hasSize(2);
        // Technology is 1000/1500 ≈ 66.67%, Chemicals is 500/1500 ≈ 33.33%
        // entries ordered by invested DESC
        assertThat(d.getEntries().get(0).getName()).isEqualTo("Technology");
        assertThat(d.getEntries().get(0).getPercentage()).isCloseTo(66.667, within(0.01));
    }

    // =========================================================================
    // getTransactions()
    // =========================================================================

    @Test
    void getTransactionsNoFilterReturnsAll() {
        IsinEntity basf = createIsin("DE000BASF111");
        createIsinName(basf, "BASF SE");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(basf, depotDeGiro, 5, 55.0);

        var txns = portfolioService.getTransactions(com.folio.dto.TransactionFilter.none());

        assertThat(txns).hasSize(2);
    }

    @Test
    void getTransactionsFilterByDepot() {
        IsinEntity basf = createIsin("DE000BASF111");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(basf, depotZero, 5, 55.0);

        var filter = new com.folio.dto.TransactionFilter(null, null, null, "DeGiro", null, null);
        var txns = portfolioService.getTransactions(filter);

        assertThat(txns).hasSize(1);
        assertThat(txns.get(0).getDepot()).isEqualTo("DeGiro");
    }

    @Test
    void getTransactionsFilterByName() {
        IsinEntity basf = createIsin("DE000BASF111");
        IsinEntity apple = createIsin("US0378331005");
        createIsinName(basf, "BASF SE");
        createIsinName(apple, "Apple Inc.");
        createTransaction(basf, depotDeGiro, 10, 50.0);
        createTransaction(apple, depotDeGiro, 5, 150.0);

        var filter = new com.folio.dto.TransactionFilter(null, null, "apple", null, null, null);
        var txns = portfolioService.getTransactions(filter);

        assertThat(txns).hasSize(1);
        assertThat(txns.get(0).getIsin()).isEqualTo(new Isin("US0378331005"));
    }
}

