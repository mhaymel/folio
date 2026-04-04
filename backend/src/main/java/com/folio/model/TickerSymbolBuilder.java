package com.folio.model;

public final class TickerSymbolBuilder {
    private Integer id;
    private IsinEntity isin;
    private String symbol;

    public TickerSymbolBuilder id(Integer id) { this.id = id; return this; }
    public TickerSymbolBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public TickerSymbolBuilder symbol(String symbol) { this.symbol = symbol; return this; }
    public TickerSymbolEntity build() { return new TickerSymbolEntity(id, isin, symbol); }
}

