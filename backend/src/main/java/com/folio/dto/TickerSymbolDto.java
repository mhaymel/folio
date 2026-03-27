package com.folio.dto;

public final class TickerSymbolDto {
    private String isin;
    private String tickerSymbol;
    private String name;

    public TickerSymbolDto() {}

    public TickerSymbolDto(String isin, String tickerSymbol, String name) {
        this.isin = isin;
        this.tickerSymbol = tickerSymbol;
        this.name = name;
    }

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static TickerSymbolDtoBuilder builder() { return new TickerSymbolDtoBuilder(); }
}

