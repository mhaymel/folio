package com.folio.model;

public final class DividendBuilder {
    private Integer id;
    private IsinEntity isin;
    private CurrencyEntity currency;
    private Double dividendPerShare;

    public DividendBuilder id(Integer id) { this.id = id; return this; }
    public DividendBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public DividendBuilder currency(CurrencyEntity currency) { this.currency = currency; return this; }
    public DividendBuilder dividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; return this; }

    public DividendEntity build() {
        DividendEntity d = new DividendEntity();
        d.setId(id); d.setIsin(isin); d.setCurrency(currency);
        d.setDividendPerShare(dividendPerShare);
        return d;
    }
}

