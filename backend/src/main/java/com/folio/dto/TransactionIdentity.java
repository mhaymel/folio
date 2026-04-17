package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link TransactionDto} incrementally via setters.
 */
public record TransactionIdentity(Integer id, String date, LocalDateTime rawDate) {
}
