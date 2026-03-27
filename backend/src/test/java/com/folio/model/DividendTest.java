package com.folio.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new Isin();
        var currency = new Currency();

        // when
        var d = Dividend.builder()
                .id(1).isin(isin).currency(currency).dividendPerShare(2.5).build();

        // then
        assertThat(d.getId()).isEqualTo(1);
        assertThat(d.getDividendPerShare()).isEqualTo(2.5);
        assertThat(d.getIsin()).isSameAs(isin);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var d = new Dividend();

        // then
        assertThat(d.getId()).isNull();
        assertThat(d.getDividendPerShare()).isNull();
    }
}

