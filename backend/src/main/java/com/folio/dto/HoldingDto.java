package com.folio.dto;

import com.folio.domain.Isin;

/**
 * {@code name} is nullable when no {@code IsinName} entity exists for the ISIN.
 */
public record HoldingDto(Isin isin, String name, Double investedAmount) {
}
