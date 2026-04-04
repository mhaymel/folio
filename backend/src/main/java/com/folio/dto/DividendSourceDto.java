package com.folio.dto;

import com.folio.domain.Isin;

public final class DividendSourceDto {
    private Isin isin;
    private String name;
    private Double estimatedAnnualIncome;

    public DividendSourceDto() {}

    public DividendSourceDto(Isin isin, String name, Double estimatedAnnualIncome) {
        this.isin = isin;
        this.name = name;
        this.estimatedAnnualIncome = estimatedAnnualIncome;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getEstimatedAnnualIncome() { return estimatedAnnualIncome; }
    public void setEstimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; }

    public static DividendSourceDtoBuilder builder() { return new DividendSourceDtoBuilder(); }
}
