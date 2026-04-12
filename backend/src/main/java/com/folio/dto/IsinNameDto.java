package com.folio.dto;

import com.folio.domain.Isin;

public final class IsinNameDto {
    private Isin isin;
    private String name;

    public IsinNameDto() {}

    public IsinNameDto(Isin isin, String name) {
        this.isin = isin;
        this.name = name;
    }

    public Isin getIsin() { return isin; }
    public void setIsin(Isin isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
