package com.folio.quote;

import static java.util.Objects.requireNonNull;

public record QuoteResult(double price, String providerName) {
    public QuoteResult {
        requireNonNull(providerName);
    }
}
