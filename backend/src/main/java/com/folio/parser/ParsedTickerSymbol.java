package com.folio.parser;

import static java.util.Objects.requireNonNull;

public record ParsedTickerSymbol(String isinCode, String symbol, String name) {
    public ParsedTickerSymbol {
        requireNonNull(isinCode);
        requireNonNull(symbol);
        requireNonNull(name);
    }
}
