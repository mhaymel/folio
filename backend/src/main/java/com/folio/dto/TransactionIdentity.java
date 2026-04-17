package com.folio.dto;

import java.time.LocalDateTime;

public record TransactionIdentity(Integer id, String date, LocalDateTime rawDate) {
}
