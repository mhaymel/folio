package com.folio.dto;

import com.folio.domain.Isin;

public record HoldingDto(Isin isin, String name, Double investedAmount) {
}