package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record StockFiltersDto(List<String> countries, List<String> branches, List<String> depots) {
    public StockFiltersDto {
        requireNonNull(countries);
        requireNonNull(branches);
        requireNonNull(depots);
        countries = List.copyOf(countries);
        branches = List.copyOf(branches);
        depots = List.copyOf(depots);
    }
}
