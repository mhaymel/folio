package com.folio.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class IsinToTickerIntegrationTest {

    private final IsinToTicker isinToTicker = new IsinToTicker();

    @Test
    void appleIsinResolvesToAAPL() {
        String appleIsin = "US0378331005";

        Optional<String> ticker = isinToTicker.tickerFor(appleIsin);
        System.out.println("AAPL -> " + ticker.orElse("NOT FOUND"));

        assertThat(ticker).isPresent().contains("AAPL");
    }

    @Test
    void microsoftIsinResolvesToMSFT() {
        String microsoftIsin = "US5949181045";

        Optional<String> ticker = isinToTicker.tickerFor(microsoftIsin);
        System.out.println("MSFT -> " + ticker.orElse("NOT FOUND"));

        assertThat(ticker).isPresent().contains("MSFT");
    }

    @Test
    void invalidIsinReturnsEmpty() {
        String bogusIsin = "XX0000000000";

        Optional<String> ticker = isinToTicker.tickerFor(bogusIsin);
        System.out.println("bogus -> " + ticker.orElse("NOT FOUND"));

        assertThat(ticker).isEmpty();
    }

    @Test
    void omvAgResolvesToTicker() {
        Optional<String> ticker = isinToTicker.tickerFor("AT0000743059");
        System.out.println("AT0000743059 -> " + ticker.orElse("NOT FOUND"));

        assertThat(ticker).isPresent();
    }

    @Test
    void batchResolvesMultipleIsinsInSingleCall() {
        List<String> isins = List.of("US0378331005", "US5949181045", "XX0000000000");

        Map<String, Optional<String>> result = isinToTicker.tickersFor(isins);
        result.forEach((isin, ticker) ->
                System.out.println(isin + " -> " + ticker.orElse("NOT FOUND")));

        assertThat(result).hasSize(3);
        assertThat(result.get("US0378331005")).contains("AAPL");
        assertThat(result.get("US5949181045")).contains("MSFT");
        assertThat(result.get("XX0000000000")).isEmpty();
    }
}