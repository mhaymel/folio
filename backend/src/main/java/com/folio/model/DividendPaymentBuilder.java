package com.folio.model;

import java.time.LocalDateTime;

public final class DividendPaymentBuilder {
    private Integer id;
    private LocalDateTime timestamp;
    private IsinEntity isin;
    private DepotEntity depot;
    private CurrencyEntity currency;
    private Double value;

    public DividendPaymentBuilder id(Integer id) { this.id = id; return this; }
    public DividendPaymentBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    public DividendPaymentBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public DividendPaymentBuilder depot(DepotEntity depot) { this.depot = depot; return this; }
    public DividendPaymentBuilder currency(CurrencyEntity currency) { this.currency = currency; return this; }
    public DividendPaymentBuilder value(Double value) { this.value = value; return this; }

    public DividendPaymentEntity build() {
        DividendPaymentEntity dp = new DividendPaymentEntity();
        dp.setId(id); dp.setTimestamp(timestamp); dp.setIsin(isin);
        dp.setDepot(depot); dp.setCurrency(currency); dp.setValue(value);
        return dp;
    }
}

