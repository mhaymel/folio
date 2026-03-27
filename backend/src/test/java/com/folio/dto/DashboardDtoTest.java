package com.folio.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class DashboardDtoTest {

    @Test
    void shouldBuildDashboardDtoViaBuilder() {
        // given
        var holdings = List.of(HoldingDto.builder()
                .isin("IE00B1").name("Test ETF").investedAmount(1000.0).build());

        // when
        DashboardDto dto = DashboardDto.builder()
                .totalPortfolioValue(5000.0)
                .stockCount(3)
                .totalDividendRatio(2.5)
                .top5Holdings(holdings)
                .top5DividendSources(List.of())
                .lastQuoteFetchAt(LocalDateTime.of(2026, 3, 27, 10, 0))
                .build();

        // then
        assertThat(dto.getTotalPortfolioValue()).isEqualTo(5000.0);
        assertThat(dto.getStockCount()).isEqualTo(3);
        assertThat(dto.getTotalDividendRatio()).isEqualTo(2.5);
        assertThat(dto.getTop5Holdings()).hasSize(1);
        assertThat(dto.getTop5DividendSources()).isEmpty();
        assertThat(dto.getLastQuoteFetchAt()).isNotNull();
    }

    @Test
    void shouldBuildHoldingDtoViaBuilder() {
        // given / when
        var dto = HoldingDto.builder()
                .isin("IE00B1").name("Test").investedAmount(500.0).build();

        // then
        assertThat(dto.getIsin()).isEqualTo("IE00B1");
        assertThat(dto.getName()).isEqualTo("Test");
        assertThat(dto.getInvestedAmount()).isEqualTo(500.0);
    }

    @Test
    void shouldBuildDividendSourceDtoViaBuilder() {
        // given / when
        var dto = DividendSourceDto.builder()
                .isin("DE0001").name("Div Stock").estimatedAnnualIncome(120.0).build();

        // then
        assertThat(dto.getIsin()).isEqualTo("DE0001");
        assertThat(dto.getEstimatedAnnualIncome()).isEqualTo(120.0);
    }
}

