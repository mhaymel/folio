package com.folio.dto;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class TransactionDtoTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        Isin isin = new Isin("IE00B4L5Y983");

        // when
        var dto = TransactionDto.builder()
                .id(1).date(LocalDateTime.of(2026, 1, 15, 10, 0))
                .isin(isin).name("ETF").depot("DeGiro")
                .count(10.0).sharePrice(50.0).build();

        // then
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getDate()).isEqualTo("15-01-2026");
        assertThat(dto.getIsin()).isEqualTo(isin);
        assertThat(dto.getCount()).isEqualTo(10.0);
        assertThat(dto.getSharePrice()).isEqualTo(50.0);
    }

    @Test
    void shouldCreateEmptyDtoViaNoArgConstructor() {
        // given / when
        var dto = new TransactionDto();

        // then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getIsin()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        // given
        var dto = new TransactionDto();
        Isin isin = new Isin("US0378331005");

        // when
        dto.setIsin(isin);
        dto.setCount(5.0);

        // then
        assertThat(dto.getIsin()).isEqualTo(isin);
        assertThat(dto.getCount()).isEqualTo(5.0);
    }
}
