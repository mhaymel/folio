package com.folio.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * Immutable value object representing a validated ISIN (International Securities Identification Number).
 * An ISIN is exactly 12 uppercase alphanumeric characters with no whitespace.
 */
public record Isin(String value) {

    public static final int ISIN_LENGTH = 12;

    /**
     * Compact constructor — validates the ISIN string.
     * Only precondition checks are allowed here per coding guidelines.
     */
    public Isin {
        requireNonNull(value, "ISIN value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ISIN must not be blank");
        }
        if (value.length() != ISIN_LENGTH) {
            throw new IllegalArgumentException(
                    "ISIN must be exactly " + ISIN_LENGTH + " characters, got " + value.length());
        }
        if (!value.equals(value.replaceAll("\\s+", ""))) {
            throw new IllegalArgumentException("ISIN must not contain whitespace");
        }
    }

    /**
     * Factory method that returns an {@link Optional} — empty if the value is not a valid ISIN.
     */
    public static Optional<Isin> of(String value) {
        if (value == null || !isValid(value)) {
            return empty();
        }
        return Optional.of(new Isin(value));
    }

    /**
     * Checks whether the given string is a valid ISIN without throwing.
     */
    public static boolean isValid(String value) {
        return value != null
                && value.length() == ISIN_LENGTH
                && !value.isBlank()
                && value.equals(value.replaceAll("\\s+", ""));
    }

    @JsonValue
    public String value() { return value; }

    @Override
    public String toString() {
        return value;
    }
}
