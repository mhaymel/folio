package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuoteSettingsDto(
        @JsonProperty("enabled") boolean isEnabled,
        Integer intervalMinutes,
        String lastFetchAt) {
}