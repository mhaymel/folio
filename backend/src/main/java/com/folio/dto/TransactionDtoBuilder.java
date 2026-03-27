package com.folio.dto;

import java.time.LocalDateTime;

public final class TransactionDtoBuilder {
    private Integer id;
    private LocalDateTime date;
    private String isin;
    private String name;
    private String depot;
    private Double count;
    private Double sharePrice;

    public TransactionDtoBuilder id(Integer id) { this.id = id; return this; }
    public TransactionDtoBuilder date(LocalDateTime date) { this.date = date; return this; }
    public TransactionDtoBuilder isin(String isin) { this.isin = isin; return this; }
    public TransactionDtoBuilder name(String name) { this.name = name; return this; }
    public TransactionDtoBuilder depot(String depot) { this.depot = depot; return this; }
    public TransactionDtoBuilder count(Double count) { this.count = count; return this; }
    public TransactionDtoBuilder sharePrice(Double sharePrice) { this.sharePrice = sharePrice; return this; }

    public TransactionDto build() {
        TransactionDto t = new TransactionDto();
        t.setId(id); t.setDate(date); t.setIsin(isin); t.setName(name);
        t.setDepot(depot); t.setCount(count); t.setSharePrice(sharePrice);
        return t;
    }
}

