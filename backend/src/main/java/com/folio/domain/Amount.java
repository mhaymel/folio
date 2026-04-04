package com.folio.domain;

import static java.util.Objects.requireNonNull;

public record Amount(double value, Currency currency) {
    public Amount {
        requireNonNull(currency);
    }
}
