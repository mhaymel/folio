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
        var quote = IsinQuoteEntity.builder()
                .id(1).isin(isin).quoteProvider(provider)
                .value(99.5).fetchedAt(LocalDateTime.now())
                .build();

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

