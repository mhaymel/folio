package com.folio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class TransactionFilterTest {

    @Test
    void shouldCreateFilterWithAllFields() {
        // given / when
        var filter = new TransactionFilter("IE00B4L5Y983", null, null, "DeGiro", null, null);

        // then
        assertThat(filter.isinFragment()).isEqualTo("IE00B4L5Y983");
        assertThat(filter.depotFragment()).isEqualTo("DeGiro");
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }

    @Test
    void shouldCreateEmptyFilterViaNone() {
        // given / when
        var filter = TransactionFilter.none();

        // then
        assertThat(filter.isinFragment()).isNull();
        assertThat(filter.nameFragment()).isNull();
        assertThat(filter.depotFragment()).isNull();
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }
}
