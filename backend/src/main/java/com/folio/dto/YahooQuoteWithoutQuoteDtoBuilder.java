package com.folio.dto;

import com.folio.domain.Isin;

public final class YahooQuoteWithoutQuoteDtoBuilder {
    private Isin isin;
    private String name;
    private String tickerSymbol;

    public YahooQuoteWithoutQuoteDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public YahooQuoteWithoutQuoteDtoBuilder name(String name) { this.name = name; return this; }
    public YahooQuoteWithoutQuoteDtoBuilder tickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; return this; }
    public YahooQuoteWithoutQuoteDto build() {
        YahooQuoteWithoutQuoteDto dto = new YahooQuoteWithoutQuoteDto();
        dto.setIsin(isin);
        dto.setName(name);
        dto.setTickerSymbol(tickerSymbol);
        return dto;
    }
}
