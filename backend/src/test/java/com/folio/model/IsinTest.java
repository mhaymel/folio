package com.folio.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class IsinTest {

    @Test
    void shouldBuildViaBuilder() {
        // given / when
        var isin = Isin.builder().id(1).isin("IE00B1").build();

        // then
        assertThat(isin.getId()).isEqualTo(1);
        assertThat(isin.getIsin()).isEqualTo("IE00B1");
    }

    @Test
    void shouldCreateViaConstructor() {
        // given / when
        var isin = new Isin(1, "IE00B1");

        // then
        assertThat(isin.getId()).isEqualTo(1);
        assertThat(isin.getIsin()).isEqualTo("IE00B1");
    }

    @Test
    void shouldSupportSetters() {
        // given
        var isin = new Isin();

        // when
        isin.setId(2);
        isin.setIsin("US0001");

        // then
        assertThat(isin.getId()).isEqualTo(2);
        assertThat(isin.getIsin()).isEqualTo("US0001");
    }
}

