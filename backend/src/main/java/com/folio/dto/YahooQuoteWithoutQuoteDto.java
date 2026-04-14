package com.folio.dto;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

import static java.util.Objects.requireNonNull;

public record YahooQuoteWithoutQuoteDto(Isin isin, String name, TickerSymbol tickerSymbol) {
    public YahooQuoteWithoutQuoteDto {
        requireNonNull(isin);
        requireNonNull(name);
        requireNonNull(tickerSymbol);
    }
}