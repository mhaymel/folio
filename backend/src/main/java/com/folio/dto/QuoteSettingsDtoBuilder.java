package com.folio.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class QuoteSettingsDtoBuilder {
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private boolean enabled;
    private Integer intervalMinutes;
    private LocalDateTime lastFetchAt;

    public QuoteSettingsDtoBuilder enabled(boolean enabled) { this.enabled = enabled; return this; }
    public QuoteSettingsDtoBuilder intervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; return this; }
    public QuoteSettingsDtoBuilder lastFetchAt(LocalDateTime lastFetchAt) { this.lastFetchAt = lastFetchAt; return this; }
    public QuoteSettingsDto build() {
        String formatted = lastFetchAt != null ? lastFetchAt.format(DATETIME_FMT) : null;
        return new QuoteSettingsDto(enabled, intervalMinutes, formatted);
    }
}
