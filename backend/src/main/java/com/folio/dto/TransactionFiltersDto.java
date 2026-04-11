package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record TransactionFiltersDto(List<String> depots) {
    public TransactionFiltersDto {
        requireNonNull(depots);
        depots = List.copyOf(depots);
    }
}
