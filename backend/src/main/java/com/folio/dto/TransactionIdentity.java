package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Groups identity and timing fields for a transaction DTO.
 */
public record TransactionIdentity(Integer id, String date, LocalDateTime rawDate) {
}

