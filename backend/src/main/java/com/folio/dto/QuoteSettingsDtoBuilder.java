package com.folio.dto;

import java.time.LocalDateTime;

public final class QuoteSettingsDtoBuilder {
    private boolean enabled;
    private Integer intervalMinutes;
    private LocalDateTime lastFetchAt;

    public QuoteSettingsDtoBuilder enabled(boolean enabled) { this.enabled = enabled; return this; }
    public QuoteSettingsDtoBuilder intervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; return this; }
    public QuoteSettingsDtoBuilder lastFetchAt(LocalDateTime lastFetchAt) { this.lastFetchAt = lastFetchAt; return this; }
    public QuoteSettingsDto build() { return new QuoteSettingsDto(enabled, intervalMinutes, lastFetchAt); }
}
