package com.folio.dto;

import static com.folio.precondition.Precondition.notNull;

import com.folio.domain.Isin;

public record IsinNameDto(Isin isin, String name) {
    public IsinNameDto {
        notNull(isin);
        notNull(name);
    }
}
