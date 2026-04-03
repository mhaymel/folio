package com.folio.domain;

import static java.util.Objects.requireNonNull;

import com.folio.model.Currency;

public record Amount(double value, Currency currency) {
    public Amount {
        requireNonNull(currency);
    }
}
