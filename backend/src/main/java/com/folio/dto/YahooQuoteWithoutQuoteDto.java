package com.folio.dto;

import com.folio.domain.Isin;

public final class YahooQuoteWithoutQuoteDto {
    private Isin isin;
    private String name;
    private String tickerSymbol;

    public YahooQuoteWithoutQuoteDto() {}

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }

    public static YahooQuoteWithoutQuoteDtoBuilder builder() { return new YahooQuoteWithoutQuoteDtoBuilder(); }
}
