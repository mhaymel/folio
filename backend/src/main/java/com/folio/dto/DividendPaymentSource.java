package com.folio.dto;

import com.folio.domain.Isin;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link DividendPaymentDto} incrementally via setters.
 */
public record DividendPaymentSource(Isin isin, String name, String depot) {
}
