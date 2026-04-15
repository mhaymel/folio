package com.folio.domain;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Factory and validation for {@link Isin} tiny type.
 * Extracted from Isin per R-014e (tiny types must not contain business logic).
 */
public final class IsinFactory {

    /**
     * Creates an Isin if the given value is valid, otherwise returns empty.
     */
    public static Optional<Isin> of(String value) {
        if (value == null || !isValid(value)) {
            return empty();
        }
        return Optional.of(new Isin(value));
    }

    /**
     * Checks whether the given value represents a valid ISIN code.
     */
    public static boolean isValid(String value) {
        return value != null
                && value.length() == Isin.ISIN_LENGTH
                && !value.isBlank()
                && value.equals(value.replaceAll("\\s+", ""));
    }
}

