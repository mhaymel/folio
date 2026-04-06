package com.folio.dto;

import com.folio.domain.Isin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class YahooQuoteWithQuoteDtoBuilder {
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Isin isin;
    private String name;
    private String tickerSymbol;
    private Double price;
    private String currency;
    private String provider;
    private LocalDateTime fetchedAt;

    public YahooQuoteWithQuoteDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public YahooQuoteWithQuoteDtoBuilder name(String name) { this.name = name; return this; }
    public YahooQuoteWithQuoteDtoBuilder tickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; return this; }
    public YahooQuoteWithQuoteDtoBuilder price(Double price) { this.price = price; return this; }
    public YahooQuoteWithQuoteDtoBuilder currency(String currency) { this.currency = currency; return this; }
    public YahooQuoteWithQuoteDtoBuilder provider(String provider) { this.provider = provider; return this; }
    public YahooQuoteWithQuoteDtoBuilder fetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; return this; }

    public YahooQuoteWithQuoteDto build() {
        YahooQuoteWithQuoteDto dto = new YahooQuoteWithQuoteDto();
        dto.setIsin(isin);
        dto.setName(name);
        dto.setTickerSymbol(tickerSymbol);
        dto.setPrice(price);
        dto.setCurrency(currency);
        dto.setProvider(provider);
        dto.setRawFetchedAt(fetchedAt);
        dto.setFetchedAt(fetchedAt != null ? fetchedAt.format(DATETIME_FMT) : null);
        return dto;
    }
}
