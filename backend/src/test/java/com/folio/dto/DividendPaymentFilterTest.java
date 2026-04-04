package com.folio.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendPaymentFilterTest {

    @Test
    void shouldCreateWithAllFields() {
        // given / when
        var filter = new DividendPaymentFilter(
            "DE000BASF111", "BASF", "DeGiro",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        // then
        assertThat(filter.isin()).isEqualTo("DE000BASF111");
        assertThat(filter.name()).isEqualTo("BASF");
        assertThat(filter.depot()).isEqualTo("DeGiro");
        assertThat(filter.fromDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(filter.toDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void shouldCreateNoneFilter() {
        // given / when
        var filter = DividendPaymentFilter.none();

        // then
        assertThat(filter.isin()).isNull();
        assertThat(filter.name()).isNull();
        assertThat(filter.depot()).isNull();
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }
}
