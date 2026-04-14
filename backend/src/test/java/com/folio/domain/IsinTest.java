package com.folio.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class IsinTest {

    @Test
    void shouldCreateValidIsin() {
        // given
        String value = "IE00B4L5Y983";

        // when
        var isin = new Isin(value);

        // then
        assertThat(isin.value()).isEqualTo("IE00B4L5Y983");
    }

    @Test
    void shouldReturnValueFromToString() {
        // given
        var isin = new Isin("IE00B4L5Y983");

        // when / then
        assertThat(isin.toString()).isEqualTo("Isin[value=IE00B4L5Y983]");
    }

    @Test
    void shouldRejectNull() {
        // given / when / then
        assertThatThrownBy(() -> new Isin(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlank() {
        // given / when / then
        assertThatThrownBy(() -> new Isin("            "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldRejectTooShort() {
        // given / when / then
        assertThatThrownBy(() -> new Isin("IE00B4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12 characters");
    }

    @Test
    void shouldRejectTooLong() {
        // given / when / then
        assertThatThrownBy(() -> new Isin("IE00B4L5Y9831"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("12 characters");
    }

    @Test
    void shouldRejectWhitespace() {
        // given / when / then
        assertThatThrownBy(() -> new Isin("IE00 B4L5Y98"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whitespace");
    }

    @Test
    void shouldReturnPresentOptionalForValidIsin() {
        // given / when
        Optional<Isin> result = Isin.of("IE00B4L5Y983");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().value()).isEqualTo("IE00B4L5Y983");
    }

    @Test
    void shouldReturnEmptyOptionalForNull() {
        // given / when
        Optional<Isin> result = Isin.of(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalForInvalidLength() {
        // given / when
        Optional<Isin> result = Isin.of("TOO_SHORT");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldValidateCorrectly() {
        // given / when / then
        assertThat(Isin.isValid("IE00B4L5Y983")).isTrue();
        assertThat(Isin.isValid("short")).isFalse();
        assertThat(Isin.isValid(null)).isFalse();
        assertThat(Isin.isValid("IE00 B4L5Y98")).isFalse();
    }

    @Test
    void shouldBeEqualForSameValue() {
        // given
        var a = new Isin("IE00B4L5Y983");
        var b = new Isin("IE00B4L5Y983");

        // when / then
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentValues() {
        // given
        var a = new Isin("IE00B4L5Y983");
        var b = new Isin("US0378331005");

        // when / then
        assertThat(a).isNotEqualTo(b);
    }
}

