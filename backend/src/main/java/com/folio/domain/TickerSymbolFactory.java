package com.folio.domain;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Factory and validation for {@link TickerSymbol} tiny type.
 * Extracted from TickerSymbol per R-014e (tiny types must not contain business logic).
 */
public final class TickerSymbolFactory {

    /**
     * Creates a TickerSymbol if the given value is valid, otherwise returns empty.
     * The value is upper-cased before construction.
     */
    public static Optional<TickerSymbol> of(String value) {
        if (value == null || !isValid(value)) {
            return empty();
        }
        return Optional.of(new TickerSymbol(value.toUpperCase()));
    }

    /**
     * Checks whether the given value can be used as a ticker symbol.
     */
    public static boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.equals(value.replaceAll("\\s+", ""));
    }
}

