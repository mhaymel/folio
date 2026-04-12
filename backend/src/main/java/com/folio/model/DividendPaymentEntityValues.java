package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Groups value fields (currency, value) for a dividend payment entity.
 */
@Embeddable
public final class DividendPaymentEntityValues {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    public DividendPaymentEntityValues() {}

    public DividendPaymentEntityValues(CurrencyEntity currency, Double value) {
        this.currency = currency;
        this.value = value;
    }

    public CurrencyEntity getCurrency() { return currency; }
    public void setCurrency(CurrencyEntity currency) { this.currency = currency; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
