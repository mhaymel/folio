package com.folio.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import static java.util.Objects.requireNonNull;

/**
 * Groups reference fields (isin, currency) for a dividend entity.
 */
@Embeddable
public final class DividendReference {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    public DividendReference() {}

    public DividendReference(IsinEntity isin, CurrencyEntity currency) {
        this.isin = requireNonNull(isin);
        this.currency = requireNonNull(currency);
    }

    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public CurrencyEntity getCurrency() { return currency; }
    public void setCurrency(CurrencyEntity currency) { this.currency = currency; }
}
