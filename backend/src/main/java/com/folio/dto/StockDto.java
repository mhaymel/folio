package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import static java.util.Objects.requireNonNull;

public record StockDto(@JsonUnwrapped SecurityIdentity security,
                       @JsonUnwrapped StockClassification classification,
                       @JsonUnwrapped StockMetrics metrics) {

    public StockDto {
        requireNonNull(security);
        requireNonNull(classification);
        requireNonNull(metrics);
    }
}