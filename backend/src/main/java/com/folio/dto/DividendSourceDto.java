package com.folio.dto;

import com.folio.domain.Isin;

import static java.util.Objects.requireNonNull;

public record DividendSourceDto(Isin isin, String name, Double estimatedAnnualIncome) {

    public DividendSourceDto {
        requireNonNull(isin);
        requireNonNull(name);
        requireNonNull(estimatedAnnualIncome);
    }
}