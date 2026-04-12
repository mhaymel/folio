package com.folio.dto;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

final class DividendPaymentDtoTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        Isin isin = new Isin("DE000BASF111");

        // when
        var dto = new DividendPaymentDto(
                new DividendPaymentIdentity(1, "15.03.2026", LocalDateTime.of(2026, 3, 15, 10, 30)),
                new DividendPaymentSource(isin, "BASF SE", "DeGiro"),
                34.0);

        // then
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getTimestamp()).isEqualTo("15.03.2026");
        assertThat(dto.getRawTimestamp()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 30));
        assertThat(dto.getIsin()).isEqualTo(isin);
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
        var dto = new DividendPaymentDto(
                new DividendPaymentIdentity(2, null, null),
                new DividendPaymentSource(null, null, null),
                null);

        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getRawTimestamp()).isNull();
    }

    @Test
    void shouldSupportSetters() {
        // given
        var dto = new DividendPaymentDto();
        Isin isin = new Isin("US0378331005");

        // when
        dto.setId(5);
        dto.setTimestamp("01.01.2026");
        dto.setIsin(isin);
        dto.setName("Apple Inc.");
        dto.setDepot("ZERO");
        dto.setValue(100.50);

        // then
        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getTimestamp()).isEqualTo("01.01.2026");
        assertThat(dto.getIsin()).isEqualTo(isin);
        assertThat(dto.getName()).isEqualTo("Apple Inc.");
        assertThat(dto.getDepot()).isEqualTo("ZERO");
        assertThat(dto.getValue()).isEqualTo(100.50);
    }
}
