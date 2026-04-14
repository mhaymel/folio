package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

import java.time.LocalDateTime;

public final class YahooQuoteWithQuoteDto {
    private SecurityIdentity security;
    private QuoteData quoteData;
    private QuoteTiming timing;

    public YahooQuoteWithQuoteDto() {
        this.security = new SecurityIdentity(null, null, null);
        this.quoteData = new QuoteData(null, null, null);
        this.timing = new QuoteTiming(null, null);
    }

    public YahooQuoteWithQuoteDto(SecurityIdentity security, QuoteData quoteData, QuoteTiming timing) {
        this.security = security;
        this.quoteData = quoteData;
        this.timing = timing;
    }

    @JsonUnwrapped
    public SecurityIdentity getSecurity() { return security; }
    @JsonUnwrapped
    public QuoteData getQuoteData() { return quoteData; }
    @JsonUnwrapped
    public QuoteTiming getTiming() { return timing; }

    public Isin getIsin() { return security.isin(); }
    public void setIsin(Isin isin) { this.security = new SecurityIdentity(isin, security.tickerSymbol(), security.name()); }
    public String getName() { return security.name(); }
    public void setName(String name) { this.security = new SecurityIdentity(security.isin(), security.tickerSymbol(), name); }
    public String getTickerSymbol() { return security.tickerSymbol() == null ? null : security.tickerSymbol().value(); }
    public void setTickerSymbol(String tickerSymbol) { this.security = new SecurityIdentity(security.isin(), TickerSymbol.of(tickerSymbol).orElse(null), security.name()); }
    public Double getPrice() { return quoteData.price(); }
    public void setPrice(Double price) { this.quoteData = new QuoteData(price, quoteData.currency(), quoteData.provider()); }
    public String getCurrency() { return quoteData.currency(); }
    public void setCurrency(String currency) { this.quoteData = new QuoteData(quoteData.price(), currency, quoteData.provider()); }
    public String getProvider() { return quoteData.provider(); }
    public void setProvider(String provider) { this.quoteData = new QuoteData(quoteData.price(), quoteData.currency(), provider); }
    public String getFetchedAt() { return timing.fetchedAt(); }
    public void setFetchedAt(String fetchedAt) { this.timing = new QuoteTiming(fetchedAt, timing.rawFetchedAt()); }
    @JsonIgnore
    public LocalDateTime getRawFetchedAt() { return timing.rawFetchedAt(); }
    public void setRawFetchedAt(LocalDateTime rawFetchedAt) { this.timing = new QuoteTiming(timing.fetchedAt(), rawFetchedAt); }

}