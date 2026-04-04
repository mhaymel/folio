package com.folio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dividend")
public final class DividendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @Column(name = "dividend_per_share", nullable = false)
    private Double dividendPerShare;

    public DividendEntity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public CurrencyEntity getCurrency() { return currency; }
    public void setCurrency(CurrencyEntity currency) { this.currency = currency; }
    public Double getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public static DividendBuilder builder() { return new DividendBuilder(); }
}