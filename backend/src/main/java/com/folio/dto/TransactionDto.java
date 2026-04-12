package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.folio.domain.Isin;

import java.time.LocalDateTime;

public final class TransactionDto {
    private TransactionIdentity identity;
    private SecurityIdentity security;
    private TransactionTradeData tradeData;

    public TransactionDto() {
        this.identity = new TransactionIdentity(null, null, null);
        this.security = new SecurityIdentity(null, null, null);
        this.tradeData = new TransactionTradeData(null, null, null);
    }

    public TransactionDto(TransactionIdentity identity, SecurityIdentity security, TransactionTradeData tradeData) {
        this.identity = identity;
        this.security = security;
        this.tradeData = tradeData;
    }

    @JsonUnwrapped
    public TransactionIdentity getIdentity() { return identity; }
    @JsonUnwrapped
    public SecurityIdentity getSecurity() { return security; }
    @JsonUnwrapped
    public TransactionTradeData getTradeData() { return tradeData; }

    public Integer getId() { return identity.id(); }
    public void setId(Integer id) { this.identity = new TransactionIdentity(id, identity.date(), identity.rawDate()); }
    public String getDate() { return identity.date(); }
    public void setDate(String date) { this.identity = new TransactionIdentity(identity.id(), date, identity.rawDate()); }
    @JsonIgnore
    public LocalDateTime getRawDate() { return identity.rawDate(); }
    public void setRawDate(LocalDateTime rawDate) { this.identity = new TransactionIdentity(identity.id(), identity.date(), rawDate); }
    public Isin getIsin() { return security.isin(); }
    public void setIsin(Isin isin) { this.security = new SecurityIdentity(isin, security.tickerSymbol(), security.name()); }
    public String getTickerSymbol() { return security.tickerSymbol(); }
    public void setTickerSymbol(String tickerSymbol) { this.security = new SecurityIdentity(security.isin(), tickerSymbol, security.name()); }
    public String getName() { return security.name(); }
    public void setName(String name) { this.security = new SecurityIdentity(security.isin(), security.tickerSymbol(), name); }
    public String getDepot() { return tradeData.depot(); }
    public void setDepot(String depot) { this.tradeData = new TransactionTradeData(depot, tradeData.count(), tradeData.sharePrice()); }
    public Double getCount() { return tradeData.count(); }
    public void setCount(Double count) { this.tradeData = new TransactionTradeData(tradeData.depot(), count, tradeData.sharePrice()); }
    public Double getSharePrice() { return tradeData.sharePrice(); }
    public void setSharePrice(Double sharePrice) { this.tradeData = new TransactionTradeData(tradeData.depot(), tradeData.count(), sharePrice); }

}
