package com.folio.parser;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public record ParsedDividendPayment(LocalDateTime date, String isinCode, String currencyCode, double amount) {
    public ParsedDividendPayment {
        requireNonNull(date);
        requireNonNull(isinCode);
        requireNonNull(currencyCode);
    }
}
