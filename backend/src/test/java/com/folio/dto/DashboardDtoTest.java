package com.folio.dto;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class DashboardDtoTest {

    private static final Isin ISIN_ETF  = new Isin("IE00B4L5Y983");
    private static final Isin ISIN_BASF = new Isin("DE000BASF111");

    @Test
    void shouldBuildDashboardDtoViaBuilder() {
        // given
        var holdings = List.of(HoldingDto.builder()
                .isin(ISIN_ETF).name("Test ETF").investedAmount(1000.0).build());

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
        assertThat(dto.getLastQuoteFetchAt()).isEqualTo("27.03.2026 10:00");
    }

    @Test
    void shouldBuildHoldingDtoViaBuilder() {
        // given / when
        var dto = HoldingDto.builder()
                .isin(ISIN_ETF).name("Test").investedAmount(500.0).build();

        // then
        assertThat(dto.getIsin()).isEqualTo(ISIN_ETF);
        assertThat(dto.getName()).isEqualTo("Test");
        assertThat(dto.getInvestedAmount()).isEqualTo(500.0);
    }

    @Test
    void shouldBuildDividendSourceDtoViaBuilder() {
        // given / when
        var dto = DividendSourceDto.builder()
                .isin(ISIN_BASF).name("Div Stock").estimatedAnnualIncome(120.0).build();

        // then
        assertThat(dto.getIsin()).isEqualTo(ISIN_BASF);
        assertThat(dto.getEstimatedAnnualIncome()).isEqualTo(120.0);
    }
}
