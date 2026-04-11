package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Groups trade/financial repositories used by import operations.
 */
@Component
record TradeImportRepositories(DepotRepository depot, TransactionRepository transaction,
                               DividendPaymentRepository dividendPayment, DividendRepository dividend) {
    TradeImportRepositories {
        requireNonNull(depot);
        requireNonNull(transaction);
        requireNonNull(dividendPayment);
        requireNonNull(dividend);
    }
}

