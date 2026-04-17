package com.folio.dto;

import java.util.List;

public record DiversificationDto(List<DiversificationEntry> entries, Double totalInvested) {
}