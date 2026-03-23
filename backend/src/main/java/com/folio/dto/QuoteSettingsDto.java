package com.folio.dto;

import java.time.LocalDateTime;

public class QuoteSettingsDto {
    private Integer intervalMinutes;
    private LocalDateTime lastFetchAt;

    public QuoteSettingsDto() {}

    public QuoteSettingsDto(Integer intervalMinutes, LocalDateTime lastFetchAt) {
        this.intervalMinutes = intervalMinutes;
        this.lastFetchAt = lastFetchAt;
    }

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public LocalDateTime getLastFetchAt() { return lastFetchAt; }
    public void setLastFetchAt(LocalDateTime lastFetchAt) { this.lastFetchAt = lastFetchAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer intervalMinutes;
        private LocalDateTime lastFetchAt;
        public Builder intervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; return this; }
        public Builder lastFetchAt(LocalDateTime lastFetchAt) { this.lastFetchAt = lastFetchAt; return this; }
        public QuoteSettingsDto build() { return new QuoteSettingsDto(intervalMinutes, lastFetchAt); }
    }
}