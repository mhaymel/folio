    package com.folio.dto;

public final class DividendSourceDtoBuilder {
    private String isin;
    private String name;
    private Double estimatedAnnualIncome;

    public DividendSourceDtoBuilder isin(String isin) { this.isin = isin; return this; }
    public DividendSourceDtoBuilder name(String name) { this.name = name; return this; }
    public DividendSourceDtoBuilder estimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; return this; }
    public DividendSourceDto build() { return new DividendSourceDto(isin, name, estimatedAnnualIncome); }
}

