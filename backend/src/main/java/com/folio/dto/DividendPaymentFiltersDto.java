package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record DividendPaymentFiltersDto(List<String> depots) {
    public DividendPaymentFiltersDto {
        requireNonNull(depots);
        depots = List.copyOf(depots);
    }
}
