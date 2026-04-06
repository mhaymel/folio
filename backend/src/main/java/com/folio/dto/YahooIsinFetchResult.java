package com.folio.dto;

import java.util.List;

public record YahooIsinFetchResult(
        List<YahooIsinWithTickerItem> withTicker,
        List<YahooIsinWithoutTickerItem> withoutTicker,
        List<YahooIsinDuplicateTickerItem> duplicateTickers) {}
