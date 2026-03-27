package com.folio.dto;

import java.time.LocalDateTime;

public final class TransactionDto {
    private Integer id;
    private LocalDateTime date;
    private String isin;
    private String name;
    private String depot;
    private Double count;
    private Double sharePrice;

    public TransactionDto() {}


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepot() { return depot; }
    public void setDepot(String depot) { this.depot = depot; }
    public Double getCount() { return count; }
    public void setCount(Double count) { this.count = count; }
    public Double getSharePrice() { return sharePrice; }
    public void setSharePrice(Double sharePrice) { this.sharePrice = sharePrice; }

    public static TransactionDtoBuilder builder() { return new TransactionDtoBuilder(); }
}