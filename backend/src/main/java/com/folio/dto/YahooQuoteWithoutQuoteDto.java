package com.folio.dto;

import com.folio.domain.Isin;

import static java.util.Objects.requireNonNull;

public record YahooQuoteWithoutQuoteDto(Isin isin, String name, String tickerSymbol) {
    public YahooQuoteWithoutQuoteDto {
        requireNonNull(isin);
        requireNonNull(name);
        requireNonNull(tickerSymbol);
    }
}
