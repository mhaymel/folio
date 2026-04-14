package com.folio.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static com.folio.precondition.Throw.illegalArgument;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

public record Isin(String value) {

    public static final int ISIN_LENGTH = 12;

    public Isin {
        requireNonNull(value);
        if (value.isBlank()) {
            illegalArgument("ISIN must not be blank");
        }
        if (value.length() != ISIN_LENGTH) {
            illegalArgument("ISIN must be exactly %s characters, got %s", ISIN_LENGTH, value.length());
        }
        if (!value.equals(value.replaceAll("\\s+", ""))) {
            illegalArgument("ISIN must not contain whitespace");
        }
    }

    public static Optional<Isin> of(String value) {
        if (value == null || !isValid(value)) {
            return empty();
        }
        return Optional.of(new Isin(value));
    }

    public static boolean isValid(String value) {
        return value != null
                && value.length() == ISIN_LENGTH
                && !value.isBlank()
                && value.equals(value.replaceAll("\\s+", ""));
    }

    @JsonValue
    public String value() { return value; }
}
