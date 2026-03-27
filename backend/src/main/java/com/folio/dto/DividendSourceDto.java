package com.folio.dto;

public final class DividendSourceDto {
    private String isin;
    private String name;
    private Double estimatedAnnualIncome;

    public DividendSourceDto() {}

    public DividendSourceDto(String isin, String name, Double estimatedAnnualIncome) {
        this.isin = isin;
        this.name = name;
        this.estimatedAnnualIncome = estimatedAnnualIncome;
    }

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getEstimatedAnnualIncome() { return estimatedAnnualIncome; }
    public void setEstimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; }

    public static DividendSourceDtoBuilder builder() { return new DividendSourceDtoBuilder(); }
}

