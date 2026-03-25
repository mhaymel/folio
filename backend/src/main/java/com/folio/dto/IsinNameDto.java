package com.folio.dto;

public class IsinNameDto {
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String isin;
        private String name;
        public Builder isin(String isin) { this.isin = isin; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public IsinNameDto build() { return new IsinNameDto(isin, name); }
    }
}

