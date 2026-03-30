package com.folio.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendPaymentDtoTest {

    @Test
    void shouldBuildViaBuilder() {
        var dto = DividendPaymentDto.builder()
                .id(1)
                .timestamp(LocalDateTime.of(2026, 3, 15, 10, 30))
                .isin("DE000BASF111")
                .name("BASF SE")
                .depot("DeGiro")
                .value(34.0)
                .build();

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getTimestamp()).isEqualTo("15.03.2026");
        assertThat(dto.getRawTimestamp()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 30));
        assertThat(dto.getIsin()).isEqualTo("DE000BASF111");
        assertThat(dto.getName()).isEqualTo("BASF SE");
        assertThat(dto.getDepot()).isEqualTo("DeGiro");
        assertThat(dto.getValue()).isEqualTo(34.0);
    }

    @Test
    void shouldCreateEmptyViaNoArgConstructor() {
        var dto = new DividendPaymentDto();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getValue()).isNull();
    }

    @Test
    void shouldFormatTimestampWithNullDate() {
        var dto = DividendPaymentDto.builder()
                .id(2)
                .timestamp(null)
                .build();

        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getRawTimestamp()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        var dto = new DividendPaymentDto();
        dto.setId(5);
        dto.setTimestamp("01.01.2026");
        dto.setIsin("US0378331005");
        dto.setName("Apple Inc.");
        dto.setDepot("ZERO");
        dto.setValue(100.50);

        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getTimestamp()).isEqualTo("01.01.2026");
        assertThat(dto.getIsin()).isEqualTo("US0378331005");
        assertThat(dto.getName()).isEqualTo("Apple Inc.");
        assertThat(dto.getDepot()).isEqualTo("ZERO");
        assertThat(dto.getValue()).isEqualTo(100.50);
    }
}