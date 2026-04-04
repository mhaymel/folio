package com.folio.quote;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

final class IsinsQuoteLoaderTest {

    private static final Isin ISIN = new Isin("IE00B4L5Y983");

    @Test
    void shouldResolveIsinFromFirstSource() {
        // given
        QuoteSource source = mock(QuoteSource.class);
        when(source.providerName()).thenReturn("TestProvider");
        when(source.fetchQuote(ISIN)).thenReturn(Optional.of(100.0));
        var loader = new IsinsQuoteLoader(List.of(source));

        // when
        Map<Isin, IsinsQuoteLoader.QuoteResult> results = loader.fetchQuotes(Set.of(ISIN));

        // then
        assertThat(results).containsKey(ISIN);
        assertThat(results.get(ISIN).price()).isEqualTo(100.0);
        assertThat(results.get(ISIN).providerName()).isEqualTo("TestProvider");
    }

    @Test
    void shouldFallbackToSecondSourceWhenFirstFails() {
        // given
        QuoteSource first = mock(QuoteSource.class);
        when(first.providerName()).thenReturn("First");
        when(first.fetchQuote(ISIN)).thenReturn(Optional.empty());

        QuoteSource second = mock(QuoteSource.class);
        when(second.providerName()).thenReturn("Second");
        when(second.fetchQuote(ISIN)).thenReturn(Optional.of(50.0));

        var loader = new IsinsQuoteLoader(List.of(first, second));

        // when
        Map<Isin, IsinsQuoteLoader.QuoteResult> results = loader.fetchQuotes(Set.of(ISIN));

        // then
        assertThat(results.get(ISIN).providerName()).isEqualTo("Second");
    }

    @Test
    void shouldNotTrySecondSourceForAlreadyResolvedIsin() {
        // given
        QuoteSource first = mock(QuoteSource.class);
        when(first.providerName()).thenReturn("First");
        when(first.fetchQuote(ISIN)).thenReturn(Optional.of(100.0));

        QuoteSource second = mock(QuoteSource.class);
        when(second.providerName()).thenReturn("Second");

        var loader = new IsinsQuoteLoader(List.of(first, second));

        // when
        loader.fetchQuotes(Set.of(ISIN));

        // then
        verify(second, never()).fetchQuote(ISIN);
    }

    @Test
    void shouldReturnEmptyMapWhenNoIsins() {
        // given
        var loader = new IsinsQuoteLoader(List.of());

        // when
        Map<Isin, IsinsQuoteLoader.QuoteResult> results = loader.fetchQuotes(Set.of());

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleExceptionInSourceGracefully() {
        // given
        QuoteSource failing = mock(QuoteSource.class);
        when(failing.providerName()).thenReturn("Failing");
        when(failing.fetchQuote(ISIN)).thenThrow(new RuntimeException("network error"));

        var loader = new IsinsQuoteLoader(List.of(failing));

        // when
        Map<Isin, IsinsQuoteLoader.QuoteResult> results = loader.fetchQuotes(Set.of(ISIN));

        // then
        assertThat(results).isEmpty();
    }
}
