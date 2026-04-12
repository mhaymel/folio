package com.folio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dividend")
public final class DividendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private DividendReference reference;

    @Column(name = "dividend_per_share", nullable = false)
    private Double dividendPerShare;

    public DividendEntity() {
        this.reference = new DividendReference();
    }

    public DividendEntity(Integer id, DividendReference reference, Double dividendPerShare) {
        this.id = id;
        this.reference = reference;
        this.dividendPerShare = dividendPerShare;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IsinEntity getIsin() { return reference.getIsin(); }
    public void setIsin(IsinEntity isin) { reference.setIsin(isin); }
    public CurrencyEntity getCurrency() { return reference.getCurrency(); }
    public void setCurrency(CurrencyEntity currency) { reference.setCurrency(currency); }
    public Double getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; }

}