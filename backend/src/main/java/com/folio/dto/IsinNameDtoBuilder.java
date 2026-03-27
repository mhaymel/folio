package com.folio.dto;

public final class IsinNameDtoBuilder {
    private String isin;
    private String name;

    public IsinNameDtoBuilder isin(String isin) { this.isin = isin; return this; }
    public IsinNameDtoBuilder name(String name) { this.name = name; return this; }
    public IsinNameDto build() { return new IsinNameDto(isin, name); }
}

