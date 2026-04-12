package com.folio.dto;

import com.folio.domain.Isin;

public final class TickerSymbolDto {
    private Isin isin;
    private String tickerSymbol;
    private String name;

    public TickerSymbolDto() {}

    public TickerSymbolDto(Isin isin, String tickerSymbol, String name) {
        this.isin = isin;
        this.tickerSymbol = tickerSymbol;
        this.name = name;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
