package com.folio.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"transaction\"")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"date\"", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isin_id", nullable = false)
    private Isin isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @Column(name = "\"count\"", nullable = false)
    private Double count;

    @Column(name = "share_price", nullable = false)
    private Double sharePrice;

    public Transaction() {}

    public Transaction(Integer id, LocalDateTime date, Isin isin, Depot depot, Double count, Double sharePrice) {
        this.id = id;
        this.date = date;
        this.isin = isin;
        this.depot = depot;
        this.count = count;
        this.sharePrice = sharePrice;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public Depot getDepot() { return depot; }
    public void setDepot(Depot depot) { this.depot = depot; }
    public Double getCount() { return count; }
    public void setCount(Double count) { this.count = count; }
    public Double getSharePrice() { return sharePrice; }
    public void setSharePrice(Double sharePrice) { this.sharePrice = sharePrice; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private LocalDateTime date;
        private Isin isin;
        private Depot depot;
        private Double count;
        private Double sharePrice;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder date(LocalDateTime date) { this.date = date; return this; }
        public Builder isin(Isin isin) { this.isin = isin; return this; }
        public Builder depot(Depot depot) { this.depot = depot; return this; }
        public Builder count(Double count) { this.count = count; return this; }
        public Builder sharePrice(Double sharePrice) { this.sharePrice = sharePrice; return this; }
        public Transaction build() { return new Transaction(id, date, isin, depot, count, sharePrice); }
    }
}