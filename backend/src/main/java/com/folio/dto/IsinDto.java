package com.folio.dto;

import com.folio.domain.Isin;

public final class IsinDto {
    private Isin isin;
    private String tickerSymbol;
    private String name;
    private String country;
    private String branch;

    public IsinDto() {}

    public IsinDto(Isin isin, String tickerSymbol, String name, String country, String branch) {
        this.isin = isin;
        this.tickerSymbol = tickerSymbol;
        this.name = name;
        this.country = country;
        this.branch = branch;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public static IsinDtoBuilder builder() { return new IsinDtoBuilder(); }
}
