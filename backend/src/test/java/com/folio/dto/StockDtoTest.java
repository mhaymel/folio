package com.folio.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class StockDtoTest {

    @Test
    void shouldBuildViaBuilder() {
        // given / when
        var dto = StockDto.builder()
                .isin("IE00B1").name("ETF").country("Ireland").branch("Tech")
                .totalShares(100.0).avgEntryPrice(50.0)
                .currentQuote(55.0).performancePercent(10.0)
                .dividendPerShare(1.5).estimatedAnnualIncome(150.0)
                .build();

        // then
        assertThat(dto.getIsin()).isEqualTo("IE00B1");
        assertThat(dto.getTotalShares()).isEqualTo(100.0);
        assertThat(dto.getPerformancePercent()).isEqualTo(10.0);
        assertThat(dto.getEstimatedAnnualIncome()).isEqualTo(150.0);
    }

    @Test
    void shouldCreateEmptyDtoViaNoArgConstructor() {
        // given / when
        var dto = new StockDto();

        // then
        assertThat(dto.getIsin()).isNull();
        assertThat(dto.getTotalShares()).isNull();
    }
}

