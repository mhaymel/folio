package com.folio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dividend_payment")
public class DividendPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"timestamp\"", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isin_id", nullable = false)
    private Isin isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    public DividendPayment() {}

    public DividendPayment(Integer id, LocalDateTime timestamp, Isin isin, Depot depot, Currency currency, Double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.isin = isin;
        this.depot = depot;
        this.currency = currency;
        this.value = value;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public Depot getDepot() { return depot; }
    public void setDepot(Depot depot) { this.depot = depot; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private LocalDateTime timestamp;
        private Isin isin;
        private Depot depot;
        private Currency currency;
        private Double value;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder isin(Isin isin) { this.isin = isin; return this; }
        public Builder depot(Depot depot) { this.depot = depot; return this; }
        public Builder currency(Currency currency) { this.currency = currency; return this; }
        public Builder value(Double value) { this.value = value; return this; }
        public DividendPayment build() { return new DividendPayment(id, timestamp, isin, depot, currency, value); }
    }
}