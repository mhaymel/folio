package com.folio.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class TransactionTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        var isin = new Isin();
        isin.setIsin("IE00B1");
        var depot = new Depot();
        depot.setName("DeGiro");

        // when
        var tx = Transaction.builder()
                .id(1).date(LocalDateTime.of(2026, 1, 1, 10, 0))
                .isin(isin).depot(depot)
                .count(10.0).sharePrice(50.0)
                .build();

        // then
        assertThat(tx.getId()).isEqualTo(1);
        assertThat(tx.getIsin().getIsin()).isEqualTo("IE00B1");
        assertThat(tx.getDepot().getName()).isEqualTo("DeGiro");
        assertThat(tx.getCount()).isEqualTo(10.0);
        assertThat(tx.getSharePrice()).isEqualTo(50.0);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        // given / when
        var tx = new Transaction();

        // then
        assertThat(tx.getId()).isNull();
        assertThat(tx.getCount()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        // given
        var tx = new Transaction();

        // when
        tx.setCount(5.0);
        tx.setSharePrice(100.0);

        // then
        assertThat(tx.getCount()).isEqualTo(5.0);
        assertThat(tx.getSharePrice()).isEqualTo(100.0);
    }
}

