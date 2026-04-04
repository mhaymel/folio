package com.folio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class TransactionFilterTest {

    @Test
    void shouldCreateFilterWithAllFields() {
        // given / when
        var filter = new TransactionFilter("IE00B4L5Y983", null, "DeGiro", null, null);

        // then
        assertThat(filter.isin()).isEqualTo("IE00B4L5Y983");
        assertThat(filter.depot()).isEqualTo("DeGiro");
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }

    @Test
    void shouldCreateEmptyFilterViaNone() {
        // given / when
        var filter = TransactionFilter.none();

        // then
        assertThat(filter.isin()).isNull();
        assertThat(filter.name()).isNull();
        assertThat(filter.depot()).isNull();
        assertThat(filter.fromDate()).isNull();
        assertThat(filter.toDate()).isNull();
    }
}
