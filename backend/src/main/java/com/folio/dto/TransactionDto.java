package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public final class TransactionDto {
    private Integer id;
    private String date;
    private LocalDateTime rawDate;
    private String isin;
    private String name;
    private String depot;
    private Double count;
    private Double sharePrice;

    public TransactionDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    @JsonIgnore
    public LocalDateTime getRawDate() { return rawDate; }
    public void setRawDate(LocalDateTime rawDate) { this.rawDate = rawDate; }
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