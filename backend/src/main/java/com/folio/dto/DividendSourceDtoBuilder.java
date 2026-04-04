package com.folio.dto;

import com.folio.domain.Isin;

public final class DividendSourceDtoBuilder {
    private Isin isin;
    private String name;
    private Double estimatedAnnualIncome;

    public DividendSourceDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public DividendSourceDtoBuilder name(String name) { this.name = name; return this; }
    public DividendSourceDtoBuilder estimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; return this; }
    public DividendSourceDto build() { return new DividendSourceDto(isin, name, estimatedAnnualIncome); }
}
