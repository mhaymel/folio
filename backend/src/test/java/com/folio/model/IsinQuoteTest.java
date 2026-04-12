package com.folio.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class IsinQuoteTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new IsinEntity();
        var provider = new QuoteProviderEntity();

        // when
        var quote = new IsinQuoteEntity(1,
                new IsinQuoteSource(isin, provider),
                new IsinQuoteData(99.5, LocalDateTime.now(), null));

        // then
        assertThat(quote.getId()).isEqualTo(1);
        assertThat(quote.getValue()).isEqualTo(99.5);
        assertThat(quote.getIsin()).isSameAs(isin);
        assertThat(quote.getQuoteProvider()).isSameAs(provider);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var quote = new IsinQuoteEntity();

        // then
        assertThat(quote.getId()).isNull();
        assertThat(quote.getValue()).isNull();
    }
}

