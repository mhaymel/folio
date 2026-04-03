package com.test;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class IsinToTickerIntegrationTest {

    private final IsinToTicker isinToTicker = new IsinToTicker();

    @Test
    void appleIsinResolvesToAAPL() {
        // given
        String appleIsin = "US0378331005";

        // when
        Optional<String> ticker = isinToTicker.tickerFor(appleIsin);
        System.out.println("AAPL → " + ticker.orElse("NOT FOUND"));

        // then
        assertThat(ticker).isPresent().contains("AAPL");
    }

    @Test
    void microsoftIsinResolvesToMSFT() {
        // given
        String microsoftIsin = "US5949181045";

        // when
        Optional<String> ticker = isinToTicker.tickerFor(microsoftIsin);
        System.out.println("MSFT → " + ticker.orElse("NOT FOUND"));

        // then
        assertThat(ticker).isPresent().contains("MSFT");
    }

    @Test
    void invalidIsinReturnsEmpty() {
        // given
        String bogusIsin = "XX0000000000";

        // when
        Optional<String> ticker = isinToTicker.tickerFor(bogusIsin);
        System.out.println("bogus → " + ticker.orElse("NOT FOUND"));

        // then
        assertThat(ticker).isEmpty();
    }

    @Test
    void batchResolvesMultipleIsinsInSingleCall() {
        // given
        List<String> isins = List.of("US0378331005", "US5949181045", "XX0000000000");

        // when
        Map<String, Optional<String>> result = isinToTicker.tickersFor(isins);
        result.forEach((isin, ticker) ->
                System.out.println(isin + " → " + ticker.orElse("NOT FOUND")));

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get("US0378331005")).contains("AAPL");
        assertThat(result.get("US5949181045")).contains("MSFT");
        assertThat(result.get("XX0000000000")).isEmpty();
    }
}

