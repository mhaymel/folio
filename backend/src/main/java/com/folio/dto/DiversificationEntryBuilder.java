package com.folio.dto;

public final class DiversificationEntryBuilder {
    private String name;
    private Double investedAmount;
    private Double percentage;

    public DiversificationEntryBuilder name(String name) { this.name = name; return this; }
    public DiversificationEntryBuilder investedAmount(Double investedAmount) { this.investedAmount = investedAmount; return this; }
    public DiversificationEntryBuilder percentage(Double percentage) { this.percentage = percentage; return this; }
    public DiversificationEntry build() { return new DiversificationEntry(name, investedAmount, percentage); }
}

