package com.folio.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "dividend_payment")
public final class DividendPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private DividendPaymentContext context;

    @Embedded
    private DividendPaymentEntityValues paymentValues;

    public DividendPaymentEntity() {
        this.context = new DividendPaymentContext();
        this.paymentValues = new DividendPaymentEntityValues();
    }

    public DividendPaymentEntity(Integer id, DividendPaymentContext context, DividendPaymentEntityValues paymentValues) {
        this.id = id;
        this.context = requireNonNull(context);
        this.paymentValues = requireNonNull(paymentValues);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getTimestamp() { return context.getTimestamp(); }
    public void setTimestamp(LocalDateTime timestamp) { context.setTimestamp(timestamp); }
    public IsinEntity getIsin() { return context.getIsin(); }
    public void setIsin(IsinEntity isin) { context.setIsin(isin); }
    public DepotEntity getDepot() { return context.getDepot(); }
    public void setDepot(DepotEntity depot) { context.setDepot(depot); }
    public CurrencyEntity getCurrency() { return paymentValues.getCurrency(); }
    public void setCurrency(CurrencyEntity currency) { paymentValues.setCurrency(currency); }
    public Double getValue() { return paymentValues.getValue(); }
    public void setValue(Double value) { paymentValues.setValue(value); }

}