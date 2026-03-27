package com.folio.dto;

public final class IsinNameDto {
    private String isin;
    private String name;

    public IsinNameDto() {}

    public IsinNameDto(String isin, String name) {
        this.isin = isin;
        this.name = name;
    }

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static IsinNameDtoBuilder builder() { return new IsinNameDtoBuilder(); }
}

