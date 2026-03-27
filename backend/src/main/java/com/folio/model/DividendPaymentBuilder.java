package com.folio.model;

import java.time.LocalDateTime;

public final class DividendPaymentBuilder {
    private Integer id;
    private LocalDateTime timestamp;
    private Isin isin;
    private Depot depot;
    private Currency currency;
    private Double value;

    public DividendPaymentBuilder id(Integer id) { this.id = id; return this; }
    public DividendPaymentBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    public DividendPaymentBuilder isin(Isin isin) { this.isin = isin; return this; }
    public DividendPaymentBuilder depot(Depot depot) { this.depot = depot; return this; }
    public DividendPaymentBuilder currency(Currency currency) { this.currency = currency; return this; }
    public DividendPaymentBuilder value(Double value) { this.value = value; return this; }

    public DividendPayment build() {
        DividendPayment dp = new DividendPayment();
        dp.setId(id); dp.setTimestamp(timestamp); dp.setIsin(isin);
        dp.setDepot(depot); dp.setCurrency(currency); dp.setValue(value);
        return dp;
    }
}

