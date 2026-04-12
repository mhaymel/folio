package com.folio.dto;

import com.folio.domain.Isin;

/**
 * Groups the source identification for a dividend payment.
 */
public record DividendPaymentSource(Isin isin, String name, String depot) {
}

