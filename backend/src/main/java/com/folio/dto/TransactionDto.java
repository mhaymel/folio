package com.folio.dto;

import java.time.LocalDateTime;

public class TransactionDto {
    private Integer id;
    private LocalDateTime date;
    private String isin;
    private String name;
    private String depot;
    private Double count;
    private Double sharePrice;

    public TransactionDto() {}

    public TransactionDto(Integer id, LocalDateTime date, String isin, String name,
                          String depot, Double count, Double sharePrice) {
        this.id = id;
        this.date = date;
        this.isin = isin;
        this.name = name;
        this.depot = depot;
        this.count = count;
        this.sharePrice = sharePrice;
    }

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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private LocalDateTime date;
        private String isin;
        private String name;
        private String depot;
        private Double count;
        private Double sharePrice;
        public Builder id(Integer id) { this.id = id; return this; }
        public Builder date(LocalDateTime date) { this.date = date; return this; }
        public Builder isin(String isin) { this.isin = isin; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder depot(String depot) { this.depot = depot; return this; }
        public Builder count(Double count) { this.count = count; return this; }
        public Builder sharePrice(Double sharePrice) { this.sharePrice = sharePrice; return this; }
        public TransactionDto build() { return new TransactionDto(id, date, isin, name, depot, count, sharePrice); }
    }
}