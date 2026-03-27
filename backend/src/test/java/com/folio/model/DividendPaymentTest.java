package com.folio.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendPaymentTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new Isin();
        var depot = new Depot();
        var currency = new Currency();

        // when
        var dp = DividendPayment.builder()
                .id(1).timestamp(LocalDateTime.of(2026, 6, 1, 0, 0))
                .isin(isin).depot(depot).currency(currency).value(42.0)
                .build();

        // then
        assertThat(dp.getId()).isEqualTo(1);
        assertThat(dp.getValue()).isEqualTo(42.0);
        assertThat(dp.getIsin()).isSameAs(isin);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var dp = new DividendPayment();

        // then
        assertThat(dp.getId()).isNull();
        assertThat(dp.getValue()).isNull();
    }
}

