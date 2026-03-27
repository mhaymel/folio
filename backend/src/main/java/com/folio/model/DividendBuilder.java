package com.folio.model;

public final class DividendBuilder {
    private Integer id;
    private Isin isin;
    private Currency currency;
    private Double dividendPerShare;

    public DividendBuilder id(Integer id) { this.id = id; return this; }
    public DividendBuilder isin(Isin isin) { this.isin = isin; return this; }
    public DividendBuilder currency(Currency currency) { this.currency = currency; return this; }
    public DividendBuilder dividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; return this; }

    public Dividend build() {
        Dividend d = new Dividend();
        d.setId(id); d.setIsin(isin); d.setCurrency(currency);
        d.setDividendPerShare(dividendPerShare);
        return d;
    }
}

