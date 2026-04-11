package com.folio.parser;

import static java.util.Objects.requireNonNull;

public record ParsedDividend(String isinCode, String name, String currencyCode, double dps) {
    public ParsedDividend {
        requireNonNull(isinCode);
        requireNonNull(name);
        requireNonNull(currencyCode);
    }
}
