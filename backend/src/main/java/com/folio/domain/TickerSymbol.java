package com.folio.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static com.folio.precondition.Precondition.noWhitespaces;
import static com.folio.precondition.Precondition.notEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

public record TickerSymbol(String value) {
    public TickerSymbol(String value) {
        this.value = notEmpty(noWhitespaces(requireNonNull(value))).toUpperCase();
    }

    public static Optional<TickerSymbol> of(String value) {
        if (value == null || !isValid(value)) {
            return empty();
        }
        return Optional.of(new TickerSymbol(value));
    }

    public static boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.equals(value.replaceAll("\\s+", ""));
    }

    @JsonValue
    public String value() { return value; }
}