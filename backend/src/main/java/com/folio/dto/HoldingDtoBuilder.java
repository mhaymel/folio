package com.folio.dto;

public final class HoldingDtoBuilder {
    private String isin;
    private String name;
    private Double investedAmount;

    public HoldingDtoBuilder isin(String isin) { this.isin = isin; return this; }
    public HoldingDtoBuilder name(String name) { this.name = name; return this; }
    public HoldingDtoBuilder investedAmount(Double investedAmount) { this.investedAmount = investedAmount; return this; }
    public HoldingDto build() { return new HoldingDto(isin, name, investedAmount); }
}

