package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.StockDto;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Portfolio positions")
public class StockController {

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public StockController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get current portfolio positions")
    public ResponseEntity<List<StockDto>> getStocks() {
        return ResponseEntity.ok(portfolioService.getStocks());
    }

    @GetMapping("/export")
    @Operation(summary = "Export stocks as CSV or Excel")
    public ResponseEntity<byte[]> exportStocks(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {

        List<StockDto> data = portfolioService.getStocks();

        if (country != null && !country.isBlank()) {
            data = data.stream().filter(s -> country.equals(s.getCountry())).toList();
        }
        if (branch != null && !branch.isBlank()) {
            data = data.stream().filter(s -> branch.equals(s.getBranch())).toList();
        }
        if (sortField != null && !sortField.isBlank()) {
            data = sorted(data, sortField, sortDir);
        }

        List<ExportColumn<StockDto>> columns = List.of(
                new ExportColumn<>("ISIN", StockDto::getIsin),
                new ExportColumn<>("Name", StockDto::getName),
                new ExportColumn<>("Country", StockDto::getCountry),
                new ExportColumn<>("Branch", StockDto::getBranch),
                new ExportColumn<>("Shares", StockDto::getTotalShares),
                new ExportColumn<>("Avg Price", StockDto::getAvgEntryPrice),
                new ExportColumn<>("Quote", StockDto::getCurrentQuote),
                new ExportColumn<>("Perf %", StockDto::getPerformancePercent),
                new ExportColumn<>("Div/Share", StockDto::getDividendPerShare),
                new ExportColumn<>("Est. Income", StockDto::getEstimatedAnnualIncome)
        );

        return exportService.export(new ExportRequest<>(data, columns, format, "stocks"));
    }


    private static List<StockDto> sorted(List<StockDto> data, String field, String dir) {
        Comparator<StockDto> cmp = switch (field) {
            case "isin" -> Comparator.comparing(StockDto::getIsin, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(StockDto::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "country" -> Comparator.comparing(StockDto::getCountry, Comparator.nullsLast(String::compareToIgnoreCase));
            case "branch" -> Comparator.comparing(StockDto::getBranch, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalShares" -> Comparator.comparing(StockDto::getTotalShares, Comparator.nullsLast(Double::compareTo));
            case "avgEntryPrice" -> Comparator.comparing(StockDto::getAvgEntryPrice, Comparator.nullsLast(Double::compareTo));
            case "currentQuote" -> Comparator.comparing(StockDto::getCurrentQuote, Comparator.nullsLast(Double::compareTo));
            case "performancePercent" -> Comparator.comparing(StockDto::getPerformancePercent, Comparator.nullsLast(Double::compareTo));
            case "dividendPerShare" -> Comparator.comparing(StockDto::getDividendPerShare, Comparator.nullsLast(Double::compareTo));
            case "estimatedAnnualIncome" -> Comparator.comparing(StockDto::getEstimatedAnnualIncome, Comparator.nullsLast(Double::compareTo));
            default -> null;
        };
        if (cmp == null) return data;
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return data.stream().sorted(cmp).toList();
    }
}
