package com.folio.dto;

import com.folio.domain.Isin;

public final class IsinDtoBuilder {
    private Isin isin;
    private String tickerSymbol;
    private String name;
    private String country;
    private String branch;

    public IsinDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public IsinDtoBuilder tickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; return this; }
    public IsinDtoBuilder name(String name) { this.name = name; return this; }
    public IsinDtoBuilder country(String country) { this.country = country; return this; }
    public IsinDtoBuilder branch(String branch) { this.branch = branch; return this; }

    public IsinDto build() {
        return new IsinDto(isin, tickerSymbol, name, country, branch);
    }
}
