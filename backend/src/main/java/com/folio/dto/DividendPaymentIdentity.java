package com.folio.dto;

import java.time.LocalDateTime;

public record DividendPaymentIdentity(Integer id, String timestamp, LocalDateTime rawTimestamp) {
}
