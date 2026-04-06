package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.folio.domain.Isin;

import java.time.LocalDateTime;

public final class YahooQuoteWithQuoteDto {
    private Isin isin;
    private String name;
    private String tickerSymbol;
    private Double price;
    private String currency;
    private String provider;
    private String fetchedAt;
    @JsonIgnore
    private LocalDateTime rawFetchedAt;

    public YahooQuoteWithQuoteDto() {}

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(String fetchedAt) { this.fetchedAt = fetchedAt; }
    public LocalDateTime getRawFetchedAt() { return rawFetchedAt; }
    public void setRawFetchedAt(LocalDateTime rawFetchedAt) { this.rawFetchedAt = rawFetchedAt; }

    public static YahooQuoteWithQuoteDtoBuilder builder() { return new YahooQuoteWithQuoteDtoBuilder(); }
}
