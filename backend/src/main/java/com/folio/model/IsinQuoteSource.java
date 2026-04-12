package com.folio.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

/**
 * Groups source reference fields (isin, quoteProvider) for an ISIN quote entity.
 */
@Embeddable
public final class IsinQuoteSource {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false, unique = true)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quote_provider_id", nullable = false)
    private QuoteProviderEntity quoteProvider;

    public IsinQuoteSource() {}

    public IsinQuoteSource(IsinEntity isin, QuoteProviderEntity quoteProvider) {
        this.isin = isin;
        this.quoteProvider = quoteProvider;
    }

    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public QuoteProviderEntity getQuoteProvider() { return quoteProvider; }
    public void setQuoteProvider(QuoteProviderEntity quoteProvider) { this.quoteProvider = quoteProvider; }
}
