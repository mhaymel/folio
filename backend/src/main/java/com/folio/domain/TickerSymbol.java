package com.folio.domain;

import static com.folio.precondition.Precondition.noWhitespaces;
import static com.folio.precondition.Precondition.notEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record TickerSymbol(@JsonValue String value) {
    @JsonCreator
    public TickerSymbol {
        notEmpty(value);
        noWhitespaces(value);
    }
}