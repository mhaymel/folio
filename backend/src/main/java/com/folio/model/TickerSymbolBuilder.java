package com.folio.model;

public final class TickerSymbolBuilder {
    private Integer id;
    private Isin isin;
    private String symbol;

    public TickerSymbolBuilder id(Integer id) { this.id = id; return this; }
    public TickerSymbolBuilder isin(Isin isin) { this.isin = isin; return this; }
    public TickerSymbolBuilder symbol(String symbol) { this.symbol = symbol; return this; }
    public TickerSymbol build() { return new TickerSymbol(id, isin, symbol); }
}

