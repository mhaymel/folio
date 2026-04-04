package com.folio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dividend_payment")
public final class DividendPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"timestamp\"", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "isin_id", nullable = false)
    private IsinEntity isin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_id", nullable = false)
    private DepotEntity depot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    public DividendPaymentEntity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public IsinEntity getIsin() { return isin; }
    public void setIsin(IsinEntity isin) { this.isin = isin; }
    public DepotEntity getDepot() { return depot; }
    public void setDepot(DepotEntity depot) { this.depot = depot; }
    public CurrencyEntity getCurrency() { return currency; }
    public void setCurrency(CurrencyEntity currency) { this.currency = currency; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public static DividendPaymentBuilder builder() { return new DividendPaymentBuilder(); }
}