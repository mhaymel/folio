package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record YahooIsinFetchResult(
        List<YahooIsinWithTickerItem> withTicker,
        List<YahooIsinWithoutTickerItem> withoutTicker,
        List<YahooIsinDuplicateTickerItem> duplicateTickers) {
    public YahooIsinFetchResult {
        requireNonNull(withTicker);
        requireNonNull(withoutTicker);
        requireNonNull(duplicateTickers);
        withTicker = List.copyOf(withTicker);
        withoutTicker = List.copyOf(withoutTicker);
        duplicateTickers = List.copyOf(duplicateTickers);
    }
}
