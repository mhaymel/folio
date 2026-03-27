package com.folio.model;

import java.time.LocalDateTime;

public final class TransactionBuilder {
    private Integer id;
    private LocalDateTime date;
    private Isin isin;
    private Depot depot;
    private Double count;
    private Double sharePrice;

    public TransactionBuilder id(Integer id) { this.id = id; return this; }
    public TransactionBuilder date(LocalDateTime date) { this.date = date; return this; }
    public TransactionBuilder isin(Isin isin) { this.isin = isin; return this; }
    public TransactionBuilder depot(Depot depot) { this.depot = depot; return this; }
    public TransactionBuilder count(Double count) { this.count = count; return this; }
    public TransactionBuilder sharePrice(Double sharePrice) { this.sharePrice = sharePrice; return this; }

    public Transaction build() {
        Transaction t = new Transaction();
        t.setId(id); t.setDate(date); t.setIsin(isin);
        t.setDepot(depot); t.setCount(count); t.setSharePrice(sharePrice);
        return t;
    }
}

