package com.folio.dto;

import static com.folio.precondition.Precondition.notNull;

public record DiversificationEntry(String name, Double investedAmount, Double percentage) {
    public DiversificationEntry {
        notNull(name);
        notNull(investedAmount);
        notNull(percentage);
    }
}
