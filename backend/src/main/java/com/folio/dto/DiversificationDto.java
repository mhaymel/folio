package com.folio.dto;

import static com.folio.precondition.Precondition.notNull;

import java.util.List;

public record DiversificationDto(List<DiversificationEntry> entries, Double totalInvested) {
    public DiversificationDto {
        notNull(entries);
        notNull(totalInvested);
        entries = List.copyOf(entries);
    }
}
