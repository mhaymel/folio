package com.folio.dto;

public final class HoldingDto {
    private String isin;
    private String name;
    private Double investedAmount;

    public HoldingDto() {}

    public HoldingDto(String isin, String name, Double investedAmount) {
        this.isin = isin;
        this.name = name;
        this.investedAmount = investedAmount;
    }

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

    public static HoldingDtoBuilder builder() { return new HoldingDtoBuilder(); }
}

