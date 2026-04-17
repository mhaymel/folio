package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link DividendPaymentDto} incrementally via setters.
 */
public record DividendPaymentIdentity(Integer id, String timestamp, LocalDateTime rawTimestamp) {
}
