package com.folio.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static com.folio.precondition.Throw.illegalArgument;
import static java.util.Objects.requireNonNull;

public record Isin(@JsonValue String value) {

    public static final int ISIN_LENGTH = 12;

    @JsonCreator
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
}
