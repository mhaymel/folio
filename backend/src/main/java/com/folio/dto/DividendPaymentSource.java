package com.folio.dto;

import com.folio.domain.Isin;

public record DividendPaymentSource(Isin isin, String name, String depot) {
}
