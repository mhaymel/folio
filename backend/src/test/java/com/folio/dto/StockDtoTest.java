package com.folio.dto;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class StockDtoTest {

    @Test
    void shouldBuildViaBuilder() {
        // given
        Isin isin = new Isin("IE00B4L5Y983");

        // when
        var dto = new StockDto(
                new SecurityIdentity(isin, null, "ETF"),
                new StockClassification("Ireland", "Tech", null),
                new StockMetrics(
                    new StockPosition(100.0, 50.0, 55.0),
                    new StockPerformance(10.0, 1.5, 150.0)));

        // then
        assertThat(dto.getIsin()).isEqualTo(isin);
        assertThat(dto.getCount()).isEqualTo(100.0);
        assertThat(dto.getPerformancePercent()).isEqualTo(10.0);
        assertThat(dto.getEstimatedAnnualIncome()).isEqualTo(150.0);
    }

    @Test
    void shouldCreateEmptyDtoViaNoArgConstructor() {
        // given / when
        var dto = new StockDto();

        // then
        assertThat(dto.getIsin()).isNull();
        assertThat(dto.getCount()).isNull();
    }
}
