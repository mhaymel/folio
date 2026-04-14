package com.folio.dto;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

public final class TickerSymbolDto {
    private Isin isin;
    private TickerSymbol tickerSymbol;
    private String name;

    public TickerSymbolDto() {}

    public TickerSymbolDto(Isin isin, String tickerSymbol, String name) {
        this.isin = isin;
        this.tickerSymbol = TickerSymbol.of(tickerSymbol).orElse(null);
        this.name = name;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getTickerSymbol() { return tickerSymbol == null ? null : tickerSymbol.value(); }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = TickerSymbol.of(tickerSymbol).orElse(null); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}