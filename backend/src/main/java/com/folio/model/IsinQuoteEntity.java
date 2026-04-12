package com.folio.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "isin_quote")
public final class IsinQuoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private IsinQuoteSource source;

    @Embedded
    private IsinQuoteData data;

    public IsinQuoteEntity() {
        this.source = new IsinQuoteSource();
        this.data = new IsinQuoteData();
    }

    public IsinQuoteEntity(Integer id, IsinQuoteSource source, IsinQuoteData data) {
        this.id = id;
        this.source = source;
        this.data = data;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IsinEntity getIsin() { return source.getIsin(); }
    public void setIsin(IsinEntity isin) { source.setIsin(isin); }
    public QuoteProviderEntity getQuoteProvider() { return source.getQuoteProvider(); }
    public void setQuoteProvider(QuoteProviderEntity quoteProvider) { source.setQuoteProvider(quoteProvider); }
    public Double getValue() { return data.getValue(); }
    public void setValue(Double value) { data.setValue(value); }
    public LocalDateTime getFetchedAt() { return data.getFetchedAt(); }
    public void setFetchedAt(LocalDateTime fetchedAt) { data.setFetchedAt(fetchedAt); }
    public String getCurrency() { return data.getCurrency(); }
    public void setCurrency(String currency) { data.setCurrency(currency); }

}