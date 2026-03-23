package com.folio.dto;

import java.util.List;

public class DiversificationDto {
    private List<Entry> entries;
    private Double totalInvested;

    public DiversificationDto() {}

    public DiversificationDto(List<Entry> entries, Double totalInvested) {
        this.entries = entries;
        this.totalInvested = totalInvested;
    }

    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }
    public Double getTotalInvested() { return totalInvested; }
    public void setTotalInvested(Double totalInvested) { this.totalInvested = totalInvested; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<Entry> entries;
        private Double totalInvested;
        public Builder entries(List<Entry> entries) { this.entries = entries; return this; }
        public Builder totalInvested(Double totalInvested) { this.totalInvested = totalInvested; return this; }
        public DiversificationDto build() { return new DiversificationDto(entries, totalInvested); }
    }

    public static class Entry {
        private String name;
        private Double investedAmount;
        private Double percentage;

        public Entry() {}

        public Entry(String name, Double investedAmount, Double percentage) {
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

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String name;
            private Double investedAmount;
            private Double percentage;
            public Builder name(String name) { this.name = name; return this; }
            public Builder investedAmount(Double investedAmount) { this.investedAmount = investedAmount; return this; }
            public Builder percentage(Double percentage) { this.percentage = percentage; return this; }
            public Entry build() { return new Entry(name, investedAmount, percentage); }
        }
    }
}