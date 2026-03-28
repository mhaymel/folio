package com.folio.controller;

import com.folio.dto.*;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Portfolio positions")
public class StockController {

    private static final Map<String, Comparator<StockDto>> SORT_FIELDS = Map.ofEntries(
        Map.entry("isin", SortHelper.text(StockDto::getIsin)),
        Map.entry("name", SortHelper.text(StockDto::getName)),
        Map.entry("country", SortHelper.text(StockDto::getCountry)),
        Map.entry("branch", SortHelper.text(StockDto::getBranch)),
        Map.entry("totalShares", SortHelper.number(StockDto::getTotalShares)),
        Map.entry("avgEntryPrice", SortHelper.number(StockDto::getAvgEntryPrice)),
        Map.entry("currentQuote", SortHelper.number(StockDto::getCurrentQuote)),
        Map.entry("performancePercent", SortHelper.number(StockDto::getPerformancePercent)),
        Map.entry("dividendPerShare", SortHelper.number(StockDto::getDividendPerShare)),
        Map.entry("estimatedAnnualIncome", SortHelper.number(StockDto::getEstimatedAnnualIncome))
    );

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public StockController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get current portfolio positions")
    public ResponseEntity<PaginatedResponseDto<StockDto>> getStocks(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<StockDto> data = filterAndSort(portfolioService.getStocks(), country, branch, sortField, sortDir);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for stocks")
    public ResponseEntity<StockFiltersDto> getStockFilters() {
        List<StockDto> stocks = portfolioService.getStocks();
        List<String> countries = stocks.stream()
            .map(StockDto::getCountry).filter(c -> c != null && !c.isBlank())
            .distinct().sorted().toList();
        List<String> branches = stocks.stream()
            .map(StockDto::getBranch).filter(b -> b != null && !b.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(new StockFiltersDto(countries, branches));
    }

    @GetMapping("/export")
    @Operation(summary = "Export stocks as CSV or Excel")
    public ResponseEntity<byte[]> exportStocks(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<StockDto> data = filterAndSort(portfolioService.getStocks(), country, branch, sortField, sortDir);

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

    private static List<StockDto> filterAndSort(List<StockDto> data, String country, String branch,
                                                 String sortField, String sortDir) {
        if (country != null && !country.isBlank()) {
            data = data.stream().filter(s -> country.equals(s.getCountry())).toList();
        }
        if (branch != null && !branch.isBlank()) {
            data = data.stream().filter(s -> branch.equals(s.getBranch())).toList();
        }
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
        }
        return data;
    }
}