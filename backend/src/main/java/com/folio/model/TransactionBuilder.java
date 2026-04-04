package com.folio.model;

import java.time.LocalDateTime;

public final class TransactionBuilder {
    private Integer id;
    private LocalDateTime date;
    private IsinEntity isin;
    private DepotEntity depot;
    private Double count;
    private Double sharePrice;

    public TransactionBuilder id(Integer id) { this.id = id; return this; }
    public TransactionBuilder date(LocalDateTime date) { this.date = date; return this; }
    public TransactionBuilder isin(IsinEntity isin) { this.isin = isin; return this; }
    public TransactionBuilder depot(DepotEntity depot) { this.depot = depot; return this; }
    public TransactionBuilder count(Double count) { this.count = count; return this; }
    public TransactionBuilder sharePrice(Double sharePrice) { this.sharePrice = sharePrice; return this; }

    public TransactionEntity build() {
        TransactionEntity t = new TransactionEntity();
        t.setId(id); t.setDate(date); t.setIsin(isin);
        t.setDepot(depot); t.setCount(count); t.setSharePrice(sharePrice);
        return t;
    }
}

