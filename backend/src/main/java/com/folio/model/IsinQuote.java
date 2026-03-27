package com.folio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "isin_quote")
public final class IsinQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false, unique = true)
    private Isin isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quote_provider_id", nullable = false)
    private QuoteProvider quoteProvider;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public IsinQuote() {}

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

    public static IsinQuoteBuilder builder() { return new IsinQuoteBuilder(); }
}