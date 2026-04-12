package com.folio.dto;

import com.folio.domain.Isin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

final class StockDtoTest {

    @Test
    void shouldBuildViaConstructor() {
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
        assertThat(dto.security().isin()).isEqualTo(isin);
        assertThat(dto.metrics().position().count()).isEqualTo(100.0);
        assertThat(dto.metrics().performance().performancePercent()).isEqualTo(10.0);
        assertThat(dto.metrics().performance().estimatedAnnualIncome()).isEqualTo(150.0);
    }

    @Test
    void shouldRejectNullSecurity() {
        assertThatNullPointerException().isThrownBy(() ->
                new StockDto(null, new StockClassification(null, null, null), new StockMetrics(new StockPosition(null, null, null), new StockPerformance(null, null, null))));
    }

    @Test
    void shouldRejectNullClassification() {
        assertThatNullPointerException().isThrownBy(() ->
                new StockDto(new SecurityIdentity(new Isin("IE00B4L5Y983"), null, null), null, new StockMetrics(new StockPosition(null, null, null), new StockPerformance(null, null, null))));
    }

    @Test
    void shouldRejectNullMetrics() {
        assertThatNullPointerException().isThrownBy(() ->
                new StockDto(new SecurityIdentity(new Isin("IE00B4L5Y983"), null, null), new StockClassification(null, null, null), null));
    }
}