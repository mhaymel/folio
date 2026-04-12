package com.folio.dto;

public final class QuoteSettingsDto {
    private boolean enabled;
    private Integer intervalMinutes;
    private String lastFetchAt;

    public QuoteSettingsDto() {}

    public QuoteSettingsDto(boolean enabled, Integer intervalMinutes, String lastFetchAt) {
        this.enabled = enabled;
        this.intervalMinutes = intervalMinutes;
        this.lastFetchAt = lastFetchAt;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public String getLastFetchAt() { return lastFetchAt; }
    public void setLastFetchAt(String lastFetchAt) { this.lastFetchAt = lastFetchAt; }

}