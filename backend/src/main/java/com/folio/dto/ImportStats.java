package com.folio.dto;

/**
 * Groups numeric import statistics.
 */
public record ImportStats(int imported, long durationMs) {
}

