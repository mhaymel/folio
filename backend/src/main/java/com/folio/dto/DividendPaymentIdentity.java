package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Groups identity and timing fields for a dividend payment DTO.
 */
public record DividendPaymentIdentity(Integer id, String timestamp, LocalDateTime rawTimestamp) {
}

