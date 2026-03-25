package com.folio.dto;

public class TickerSymbolDto {
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String isin;
        private String tickerSymbol;
        private String name;
        public Builder isin(String isin) { this.isin = isin; return this; }
        public Builder tickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public TickerSymbolDto build() { return new TickerSymbolDto(isin, tickerSymbol, name); }
    }
}

