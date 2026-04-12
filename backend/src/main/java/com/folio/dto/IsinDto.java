package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.folio.domain.Isin;

public final class IsinDto {
    private SecurityIdentity identity;
    private IsinClassification classification;

    public IsinDto() {
        this.identity = new SecurityIdentity(null, null, null);
        this.classification = new IsinClassification(null, null);
    }

    public IsinDto(Isin isin, String tickerSymbol, String name, String country, String branch) {
        this.identity = new SecurityIdentity(isin, tickerSymbol, name);
        this.classification = new IsinClassification(country, branch);
    }

    @JsonUnwrapped
    public SecurityIdentity getIdentity() { return identity; }
    @JsonUnwrapped
    public IsinClassification getClassification() { return classification; }

    public Isin getIsin() { return identity.isin(); }
    public void setIsin(Isin isin) { this.identity = new SecurityIdentity(isin, identity.tickerSymbol(), identity.name()); }
    public String getTickerSymbol() { return identity.tickerSymbol(); }
    public void setTickerSymbol(String tickerSymbol) { this.identity = new SecurityIdentity(identity.isin(), tickerSymbol, identity.name()); }
    public String getName() { return identity.name(); }
    public void setName(String name) { this.identity = new SecurityIdentity(identity.isin(), identity.tickerSymbol(), name); }
    public String getCountry() { return classification.country(); }
    public void setCountry(String country) { this.classification = new IsinClassification(country, classification.branch()); }
    public String getBranch() { return classification.branch(); }
    public void setBranch(String branch) { this.classification = new IsinClassification(classification.country(), branch); }

}
