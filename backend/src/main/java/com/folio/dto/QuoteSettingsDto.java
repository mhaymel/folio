package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code lastFetchAt} is nullable when no quote fetch has run yet.
 */
public record QuoteSettingsDto(
        @JsonProperty("enabled") boolean isEnabled,
        Integer intervalMinutes,
        String lastFetchAt) {
}
