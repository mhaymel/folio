package com.folio.model;

import java.time.LocalDateTime;

public final class IsinQuoteBuilder {
    private Integer id;
    private Isin isin;
    private QuoteProvider quoteProvider;
    private Double value;
    private LocalDateTime fetchedAt;

    public IsinQuoteBuilder id(Integer id) { this.id = id; return this; }
    public IsinQuoteBuilder isin(Isin isin) { this.isin = isin; return this; }
    public IsinQuoteBuilder quoteProvider(QuoteProvider quoteProvider) { this.quoteProvider = quoteProvider; return this; }
    public IsinQuoteBuilder value(Double value) { this.value = value; return this; }
    public IsinQuoteBuilder fetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; return this; }

    public IsinQuote build() {
        IsinQuote q = new IsinQuote();
        q.setId(id); q.setIsin(isin); q.setQuoteProvider(quoteProvider);
        q.setValue(value); q.setFetchedAt(fetchedAt);
        return q;
    }
}

