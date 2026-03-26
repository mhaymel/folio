package com.folio.controller;

import com.folio.dto.DashboardDto;
import com.folio.service.ExportService;
import com.folio.service.ExportService.Column;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Portfolio dashboard summary")
public class DashboardController {

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public DashboardController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
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
        List<Column<DashboardDto.HoldingDto>> columns = List.of(
                new Column<>("ISIN", DashboardDto.HoldingDto::getIsin),
                new Column<>("Name", DashboardDto.HoldingDto::getName),
                new Column<>("Invested (EUR)", DashboardDto.HoldingDto::getInvestedAmount)
        );
        return exportService.export(dto.getTop5Holdings(), columns, format, "top5-holdings");
    }

    @GetMapping("/dividends/export")
    @Operation(summary = "Export top-5 dividend sources as CSV or Excel")
    public ResponseEntity<byte[]> exportDividends(@RequestParam(defaultValue = "csv") String format) {
        DashboardDto dto = portfolioService.getDashboard();
        List<Column<DashboardDto.DividendSourceDto>> columns = List.of(
                new Column<>("ISIN", DashboardDto.DividendSourceDto::getIsin),
                new Column<>("Name", DashboardDto.DividendSourceDto::getName),
                new Column<>("Est. Annual Income (EUR)", DashboardDto.DividendSourceDto::getEstimatedAnnualIncome)
        );
        return exportService.export(dto.getTop5DividendSources(), columns, format, "top5-dividends");
    }
}