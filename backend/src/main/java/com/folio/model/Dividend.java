package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dividend")
public class Dividend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isin_id", nullable = false)
    private Isin isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "dividend_per_share", nullable = false)
    private Double dividendPerShare;

    public Dividend() {}

    public Dividend(Integer id, Isin isin, Currency currency, Double dividendPerShare) {
        this.id = id;
        this.isin = isin;
        this.currency = currency;
        this.dividendPerShare = dividendPerShare;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public Double getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private Isin isin;
        private Currency currency;
        private Double dividendPerShare;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder isin(Isin isin) { this.isin = isin; return this; }
        public Builder currency(Currency currency) { this.currency = currency; return this; }
        public Builder dividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; return this; }
        public Dividend build() { return new Dividend(id, isin, currency, dividendPerShare); }
    }
}