package com.folio.dto;

import java.time.LocalDateTime;

public record QuoteTiming(String fetchedAt, LocalDateTime rawFetchedAt) {
}
