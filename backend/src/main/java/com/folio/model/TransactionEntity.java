package com.folio.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"transaction\"")
public final class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private TransactionContext context;

    @Embedded
    private TransactionValues values;

    public TransactionEntity() {
        this.context = new TransactionContext();
        this.values = new TransactionValues();
    }

    public TransactionEntity(Integer id, TransactionContext context, TransactionValues values) {
        this.id = id;
        this.context = context;
        this.values = values;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getDate() { return context.getDate(); }
    public void setDate(LocalDateTime date) { context.setDate(date); }
    public IsinEntity getIsin() { return context.getIsin(); }
    public void setIsin(IsinEntity isin) { context.setIsin(isin); }
    public DepotEntity getDepot() { return context.getDepot(); }
    public void setDepot(DepotEntity depot) { context.setDepot(depot); }
    public Double getCount() { return values.getCount(); }
    public void setCount(Double count) { values.setCount(count); }
    public Double getSharePrice() { return values.getSharePrice(); }
    public void setSharePrice(Double sharePrice) { values.setSharePrice(sharePrice); }

}