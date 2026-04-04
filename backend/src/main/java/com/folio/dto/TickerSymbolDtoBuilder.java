package com.folio.dto;

import com.folio.domain.Isin;

public final class TickerSymbolDtoBuilder {
    private Isin isin;
    private String tickerSymbol;
    private String name;

    public TickerSymbolDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public TickerSymbolDtoBuilder tickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; return this; }
    public TickerSymbolDtoBuilder name(String name) { this.name = name; return this; }
    public TickerSymbolDto build() { return new TickerSymbolDto(isin, tickerSymbol, name); }
}
