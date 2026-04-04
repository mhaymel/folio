package com.folio.domain;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record Quote(Amount amount, Instant timestamp) {
    public Quote {
        requireNonNull(amount);
        requireNonNull(timestamp);
    }
}

