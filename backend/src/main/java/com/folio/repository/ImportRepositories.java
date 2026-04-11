package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Bundles all repositories needed by ImportService to keep its constructor within the 3-parameter limit.
 */
@Component
public final class ImportRepositories {

    private final IsinImportRepositories isinRepos;
    private final TradeImportRepositories tradeRepos;
    private final ReferenceImportRepositories referenceRepos;

    public ImportRepositories(IsinImportRepositories isinRepos, TradeImportRepositories tradeRepos,
                              ReferenceImportRepositories referenceRepos) {
        this.isinRepos = requireNonNull(isinRepos);
        this.tradeRepos = requireNonNull(tradeRepos);
        this.referenceRepos = requireNonNull(referenceRepos);
    }


    public IsinRepository isin() { return isinRepos.isin(); }
    public IsinNameRepository isinName() { return isinRepos.isinName(); }
    public DepotRepository depot() { return tradeRepos.depot(); }
    public CurrencyRepository currency() { return referenceRepos.currency(); }
    public TransactionRepository transaction() { return tradeRepos.transaction(); }
    public DividendRepository dividend() { return tradeRepos.dividend(); }
    public DividendPaymentRepository dividendPayment() { return tradeRepos.dividendPayment(); }
    public CountryRepository country() { return referenceRepos.country(); }
    public BranchRepository branch() { return referenceRepos.branch(); }
    public TickerSymbolRepository tickerSymbol() { return isinRepos.tickerSymbol(); }
}

