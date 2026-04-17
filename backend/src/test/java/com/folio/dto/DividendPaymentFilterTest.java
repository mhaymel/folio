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
        assertThat(filter.isinFragment()).isEqualTo("DE000BASF111");
        assertThat(filter.nameFragment()).isEqualTo("BASF");
        assertThat(filter.depotFragment()).isEqualTo("DeGiro");
        assertThat(filter.fromDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(filter.toDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void shouldCreateNoneFilter() {
        // given / when
        var filter = DividendPaymentFilter.none();

        // then
        assertThat(filter.isinFragment()).isEmpty();
        assertThat(filter.nameFragment()).isEmpty();
        assertThat(filter.depotFragment()).isEmpty();
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }
}
