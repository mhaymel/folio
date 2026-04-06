package com.folio.model;

import java.time.LocalDateTime;

public final class IsinQuoteBuilder {
    private Integer id;
    private IsinEntity isin;
    private QuoteProviderEntity quoteProvider;
    private Double value;
    private LocalDateTime fetchedAt;
    private String currency;

    public IsinQuoteBuilder id(Integer id) { this.id = id; return this; }
    public IsinQuoteBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public IsinQuoteBuilder quoteProvider(QuoteProviderEntity quoteProvider) { this.quoteProvider = quoteProvider; return this; }
    public IsinQuoteBuilder value(Double value) { this.value = value; return this; }
    public IsinQuoteBuilder fetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; return this; }
    public IsinQuoteBuilder currency(String currency) { this.currency = currency; return this; }

    public IsinQuoteEntity build() {
        IsinQuoteEntity q = new IsinQuoteEntity();
        q.setId(id); q.setIsin(isin); q.setQuoteProvider(quoteProvider);
        q.setValue(value); q.setFetchedAt(fetchedAt); q.setCurrency(currency);
        return q;
    }
}

