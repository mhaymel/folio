package com.folio.controller;

import com.folio.dto.DashboardDto;
import com.folio.dto.DividendSourceDto;
import com.folio.dto.HoldingDto;
import com.folio.dto.ExportRequest;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Portfolio dashboard summary")
public final class DashboardController {

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public DashboardController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = requireNonNull(portfolioService);
        this.exportService = requireNonNull(exportService);
    }

    @GetMapping
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(portfolioService.getDashboard());
    }

    @GetMapping("/holdings/export")
    @Operation(summary = "Export top-5 holdings as CSV or Excel")
    public ResponseEntity<byte[]> exportHoldings(@RequestParam(defaultValue = "csv") String format) {
        DashboardDto dto = portfolioService.getDashboard();
        List<ExportColumn<HoldingDto>> columns = List.of(
                new ExportColumn<>("ISIN", HoldingDto::getIsin),
                new ExportColumn<>("Name", HoldingDto::getName),
                new ExportColumn<>("Invested (EUR)", HoldingDto::getInvestedAmount)
        );
        return exportService.export(new ExportRequest<>(dto.getTop5Holdings(), columns, format, "top5-holdings"));
    }

    @GetMapping("/dividends/export")
    @Operation(summary = "Export top-5 dividend sources as CSV or Excel")
    public ResponseEntity<byte[]> exportDividends(@RequestParam(defaultValue = "csv") String format) {
        DashboardDto dto = portfolioService.getDashboard();
        List<ExportColumn<DividendSourceDto>> columns = List.of(
                new ExportColumn<>("ISIN", DividendSourceDto::getIsin),
                new ExportColumn<>("Name", DividendSourceDto::getName),
                new ExportColumn<>("Est. Annual Income (EUR)", DividendSourceDto::getEstimatedAnnualIncome)
        );
        return exportService.export(new ExportRequest<>(dto.getTop5DividendSources(), columns, format, "top5-dividends"));
    }
}