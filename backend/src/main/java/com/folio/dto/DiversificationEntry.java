package com.folio.dto;

public final class DiversificationEntry {
    private String name;
    private Double investedAmount;
    private Double percentage;

    public DiversificationEntry() {}

    public DiversificationEntry(String name, Double investedAmount, Double percentage) {
        this.name = name;
        this.investedAmount = investedAmount;
        this.percentage = percentage;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public static DiversificationEntryBuilder builder() { return new DiversificationEntryBuilder(); }
}

