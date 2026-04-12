package com.folio.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendPaymentTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new IsinEntity();
        var depot = new DepotEntity();
        var currency = new CurrencyEntity();

        // when
        var dp = new DividendPaymentEntity(1,
                new DividendPaymentContext(LocalDateTime.of(2026, 6, 1, 0, 0), isin, depot),
                new DividendPaymentEntityValues(currency, 42.0));

        // then
        assertThat(dp.getId()).isEqualTo(1);
        assertThat(dp.getValue()).isEqualTo(42.0);
        assertThat(dp.getIsin()).isSameAs(isin);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var dp = new DividendPaymentEntity();

        // then
        assertThat(dp.getId()).isNull();
        assertThat(dp.getValue()).isNull();
    }
}

