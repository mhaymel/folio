package com.folio.dto;

import com.folio.domain.Isin;

public final class IsinNameDtoBuilder {
    private Isin isin;
    private String name;

    public IsinNameDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public IsinNameDtoBuilder name(String name) { this.name = name; return this; }
    public IsinNameDto build() { return new IsinNameDto(isin, name); }
}
