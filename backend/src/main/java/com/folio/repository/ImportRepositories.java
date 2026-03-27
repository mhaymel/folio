package com.folio.repository;

import org.springframework.stereotype.Component;

/**
 * Bundles all repositories needed by ImportService to keep its constructor within the 3-parameter limit.
 */
@Component
public final class ImportRepositories {

    private final IsinRepository isinRepo;
    private final IsinNameRepository isinNameRepo;
    private final DepotRepository depotRepo;
    private final CurrencyRepository currencyRepo;
    private final TransactionRepository transactionRepo;
    private final DividendRepository dividendRepo;
    private final DividendPaymentRepository dividendPaymentRepo;
    private final CountryRepository countryRepo;
    private final BranchRepository branchRepo;
    private final TickerSymbolRepository tickerSymbolRepo;

    public ImportRepositories(IsinRepository isinRepo, IsinNameRepository isinNameRepo,
                              DepotRepository depotRepo, CurrencyRepository currencyRepo,
                              TransactionRepository transactionRepo, DividendRepository dividendRepo,
                              DividendPaymentRepository dividendPaymentRepo, CountryRepository countryRepo,
                              BranchRepository branchRepo, TickerSymbolRepository tickerSymbolRepo) {
        this.isinRepo = isinRepo;
        this.isinNameRepo = isinNameRepo;
        this.depotRepo = depotRepo;
        this.currencyRepo = currencyRepo;
        this.transactionRepo = transactionRepo;
        this.dividendRepo = dividendRepo;
        this.dividendPaymentRepo = dividendPaymentRepo;
        this.countryRepo = countryRepo;
        this.branchRepo = branchRepo;
        this.tickerSymbolRepo = tickerSymbolRepo;
    }

    public IsinRepository isin() { return isinRepo; }
    public IsinNameRepository isinName() { return isinNameRepo; }
    public DepotRepository depot() { return depotRepo; }
    public CurrencyRepository currency() { return currencyRepo; }
    public TransactionRepository transaction() { return transactionRepo; }
    public DividendRepository dividend() { return dividendRepo; }
    public DividendPaymentRepository dividendPayment() { return dividendPaymentRepo; }
    public CountryRepository country() { return countryRepo; }
    public BranchRepository branch() { return branchRepo; }
    public TickerSymbolRepository tickerSymbol() { return tickerSymbolRepo; }
}

