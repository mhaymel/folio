package com.folio.dto;

import com.folio.domain.Isin;

public final class HoldingDto {
    private Isin isin;
    private String name;
    private Double investedAmount;

    public HoldingDto() {}

    public HoldingDto(Isin isin, String name, Double investedAmount) {
        this.isin = isin;
        this.name = name;
        this.investedAmount = investedAmount;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

}
