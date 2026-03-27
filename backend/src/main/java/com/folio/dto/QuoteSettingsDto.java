package com.folio.dto;

import java.time.LocalDateTime;

public final class QuoteSettingsDto {
    private boolean enabled;
    private Integer intervalMinutes;
    private LocalDateTime lastFetchAt;

    public QuoteSettingsDto() {}

    public QuoteSettingsDto(boolean enabled, Integer intervalMinutes, LocalDateTime lastFetchAt) {
        this.enabled = enabled;
        this.intervalMinutes = intervalMinutes;
        this.lastFetchAt = lastFetchAt;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public LocalDateTime getLastFetchAt() { return lastFetchAt; }
    public void setLastFetchAt(LocalDateTime lastFetchAt) { this.lastFetchAt = lastFetchAt; }

    public static QuoteSettingsDtoBuilder builder() { return new QuoteSettingsDtoBuilder(); }
}