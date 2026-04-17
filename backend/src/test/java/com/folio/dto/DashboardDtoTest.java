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
        var holdings = List.of(new HoldingDto(ISIN_ETF, "Test ETF", 1000.0));

        // when
        DashboardDto dto = new DashboardDto(
                new DashboardSummary(5000.0, 3, 2.5),
                new DashboardLists(holdings, List.of()),
                "27.03.2026 10:00");

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
        var dto = new HoldingDto(ISIN_ETF, "Test", 500.0);

        // then
        assertThat(dto.isin()).isEqualTo(ISIN_ETF);
        assertThat(dto.name()).isEqualTo("Test");
        assertThat(dto.investedAmount()).isEqualTo(500.0);
    }

    @Test
    void shouldBuildDividendSourceDtoViaBuilder() {
        // given / when
        var dto = new DividendSourceDto(ISIN_BASF, "Div Stock", 120.0);

        // then
        assertThat(dto.isin()).isEqualTo(ISIN_BASF);
        assertThat(dto.estimatedAnnualIncome()).isEqualTo(120.0);
    }
}
