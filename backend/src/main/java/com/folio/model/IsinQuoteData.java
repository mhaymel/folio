package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * Groups quote data fields (value, fetchedAt, currency) for an ISIN quote entity.
 */
@Embeddable
public final class IsinQuoteData {
    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "currency", length = 3)
    private String currency;

    public IsinQuoteData() {}

    public IsinQuoteData(Double value, LocalDateTime fetchedAt, String currency) {
        this.value = requireNonNull(value);
        this.fetchedAt = requireNonNull(fetchedAt);
        this.currency = currency;
    }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
