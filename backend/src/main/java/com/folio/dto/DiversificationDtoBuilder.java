package com.folio.dto;

import java.util.List;

public final class DiversificationDtoBuilder {
    private List<DiversificationEntry> entries;
    private Double totalInvested;

    public DiversificationDtoBuilder entries(List<DiversificationEntry> entries) { this.entries = entries; return this; }
    public DiversificationDtoBuilder totalInvested(Double totalInvested) { this.totalInvested = totalInvested; return this; }
    public DiversificationDto build() { return new DiversificationDto(entries, totalInvested); }
}

