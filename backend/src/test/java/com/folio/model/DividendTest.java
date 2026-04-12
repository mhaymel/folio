package com.folio.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new IsinEntity();
        var currency = new CurrencyEntity();

        // when
        var d = new DividendEntity(1, new DividendReference(isin, currency), 2.5);

        // then
        assertThat(d.getId()).isEqualTo(1);
        assertThat(d.getDividendPerShare()).isEqualTo(2.5);
        assertThat(d.getIsin()).isSameAs(isin);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var d = new DividendEntity();

        // then
        assertThat(d.getId()).isNull();
        assertThat(d.getDividendPerShare()).isNull();
    }
}

