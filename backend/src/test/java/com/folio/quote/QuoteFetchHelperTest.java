package com.folio.quote;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class QuoteFetchHelperTest {

    @Test
    void shouldParseSimpleDecimal() {
        // given
        String raw = "123.45";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isPresent().hasValue(123.45);
    }

    @Test
    void shouldParseGermanDecimalWithComma() {
        // given
        String raw = "123,45";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isPresent().hasValue(123.45);
    }

    @Test
    void shouldParseGermanFormatWithThousandsSeparator() {
        // given
        String raw = "1.234,56";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isPresent().hasValue(1234.56);
    }

    @Test
    void shouldReturnEmptyForZero() {
        // given
        String raw = "0";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNegative() {
        // given
        String raw = "-5.00";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForInvalidString() {
        // given
        String raw = "abc";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldStripNonBreakingSpaces() {
        // given
        String raw = "\u00a0 42,50 \u00a0";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isPresent().hasValue(42.50);
    }

    @Test
    void shouldStripHtmlNbsp() {
        // given
        String raw = "&nbsp;99.99&nbsp;";

        // when
        Optional<Double> result = QuoteFetchHelper.parseDecimal(raw);

        // then
        assertThat(result).isPresent().hasValue(99.99);
    }
}

