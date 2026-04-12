package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Groups timing fields for a Yahoo quote DTO.
 */
public record QuoteTiming(String fetchedAt, LocalDateTime rawFetchedAt) {
}

