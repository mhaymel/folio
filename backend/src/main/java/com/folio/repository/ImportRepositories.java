package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

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
        this.isinRepo = requireNonNull(isinRepo);
        this.isinNameRepo = requireNonNull(isinNameRepo);
        this.depotRepo = requireNonNull(depotRepo);
        this.currencyRepo = requireNonNull(currencyRepo);
        this.transactionRepo = requireNonNull(transactionRepo);
        this.dividendRepo = requireNonNull(dividendRepo);
        this.dividendPaymentRepo = requireNonNull(dividendPaymentRepo);
        this.countryRepo = requireNonNull(countryRepo);
        this.branchRepo = requireNonNull(branchRepo);
        this.tickerSymbolRepo = requireNonNull(tickerSymbolRepo);
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

