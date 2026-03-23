package com.folio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "isin_quote")
public class IsinQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isin_id", nullable = false, unique = true)
    private Isin isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_provider_id", nullable = false)
    private QuoteProvider quoteProvider;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public IsinQuote() {}

    public IsinQuote(Integer id, Isin isin, QuoteProvider quoteProvider, Double value, LocalDateTime fetchedAt) {
        this.id = id;
        this.isin = isin;
        this.quoteProvider = quoteProvider;
        this.value = value;
        this.fetchedAt = fetchedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public QuoteProvider getQuoteProvider() { return quoteProvider; }
    public void setQuoteProvider(QuoteProvider quoteProvider) { this.quoteProvider = quoteProvider; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private Isin isin;
        private QuoteProvider quoteProvider;
        private Double value;
        private LocalDateTime fetchedAt;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder isin(Isin isin) { this.isin = isin; return this; }
        public Builder quoteProvider(QuoteProvider quoteProvider) { this.quoteProvider = quoteProvider; return this; }
        public Builder value(Double value) { this.value = value; return this; }
        public Builder fetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; return this; }
        public IsinQuote build() { return new IsinQuote(id, isin, quoteProvider, value, fetchedAt); }
    }
}