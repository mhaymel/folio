package com.folio.controller;

import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.StockDto;
import com.folio.dto.StockFiltersDto;
import com.folio.dto.StockPaginatedResponseDto;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stocks", description = "Portfolio positions aggregated across all depots")
final class StocksController {

    private static final Map<String, Comparator<StockDto>> SORT_FIELDS = Map.ofEntries(
        Map.entry("isin", SortHelper.text(s -> s.security().isin() == null ? null : s.security().isin().value())),
        Map.entry("tickerSymbol", SortHelper.text(s -> s.security().tickerSymbol() == null ? null : s.security().tickerSymbol().value())),
        Map.entry("name", SortHelper.text(s -> s.security().name())),
        Map.entry("country", SortHelper.text(s -> s.classification().country())),
        Map.entry("branch", SortHelper.text(s -> s.classification().branch())),
        Map.entry("count", SortHelper.number(s -> s.metrics().position().count())),
        Map.entry("avgEntryPrice", SortHelper.number(s -> s.metrics().position().avgEntryPrice())),
        Map.entry("currentQuote", SortHelper.number(s -> s.metrics().position().currentQuote())),
        Map.entry("performancePercent", SortHelper.number(s -> s.metrics().performance().performancePercent())),
        Map.entry("dividendPerShare", SortHelper.number(s -> s.metrics().performance().dividendPerShare())),
        Map.entry("estimatedAnnualIncome", SortHelper.number(s -> s.metrics().performance().estimatedAnnualIncome()))
    );

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public StocksController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = requireNonNull(portfolioService);
        this.exportService = requireNonNull(exportService);
    }

    @GetMapping
    @Operation(summary = "Get current portfolio positions aggregated across all depots")
    public ResponseEntity<StockPaginatedResponseDto> getStocks(
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String tickerSymbol,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<StockDto> data = filterAndSort(portfolioService.getAggregatedStocks(),
            new StockFilter(isin, tickerSymbol, name, null, country, branch),
            new SortRequest(sortField, sortDir));
        double sumCount = data.stream().mapToDouble(s -> s.metrics().position().count()).sum();
        PaginatedResponseDto<StockDto> paginated = PaginationHelper.paginate(data, page, pageSize);
        return ResponseEntity.ok(new StockPaginatedResponseDto(
            paginated.getItems(), paginated.getPage(), paginated.getPageSize(),
            paginated.getTotalItems(), paginated.getTotalPages(), sumCount));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for aggregated stocks")
    public ResponseEntity<StockFiltersDto> getStockFilters() {
        List<StockDto> stocks = portfolioService.getAggregatedStocks();
        List<String> countries = stocks.stream()
            .map(s -> s.classification().country()).filter(c -> c != null && !c.isBlank())
            .distinct().sorted().toList();
        List<String> branches = stocks.stream()
            .map(s -> s.classification().branch()).filter(b -> b != null && !b.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(new StockFiltersDto(countries, branches, List.of()));
    }

    @GetMapping("/export")
    @Operation(summary = "Export aggregated stocks as CSV or Excel")
    public ResponseEntity<byte[]> exportStocks(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String tickerSymbol,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<StockDto> data = filterAndSort(portfolioService.getAggregatedStocks(),
            new StockFilter(isin, tickerSymbol, name, null, country, branch),
            new SortRequest(sortField, sortDir));

        List<ExportColumn<StockDto>> columns = List.of(
                new ExportColumn<>("ISIN", s -> s.security().isin()),
                new ExportColumn<>("Ticker", s -> s.security().tickerSymbol() == null ? null : s.security().tickerSymbol().value()),
                new ExportColumn<>("Name", s -> s.security().name()),
                new ExportColumn<>("CountryEntity", s -> s.classification().country()),
                new ExportColumn<>("BranchEntity", s -> s.classification().branch()),
                new ExportColumn<>("Count", s -> s.metrics().position().count()),
                new ExportColumn<>("Avg Price", s -> s.metrics().position().avgEntryPrice()),
                new ExportColumn<>("Quote", s -> s.metrics().position().currentQuote()),
                new ExportColumn<>("Perf %", s -> s.metrics().performance().performancePercent()),
                new ExportColumn<>("Div/Share", s -> s.metrics().performance().dividendPerShare()),
                new ExportColumn<>("Est. Income", s -> s.metrics().performance().estimatedAnnualIncome())
        );

        return exportService.export(new ExportRequest<>(data, columns, format, "stocks"));
    }

    private static Set<String> splitMultiValue(String param) {
        if (param == null || param.isBlank()) return Set.of();
        return Arrays.stream(param.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    private static List<StockDto> filterAndSort(List<StockDto> data, StockFilter filter,
                                                 SortRequest sort) {
        if (filter.isin() != null && !filter.isin().isBlank()) {
            String lower = filter.isin().toLowerCase();
            data = data.stream().filter(s -> s.security().isin() != null && s.security().isin().value().toLowerCase().contains(lower)).toList();
        }
        if (filter.tickerSymbol() != null && !filter.tickerSymbol().isBlank()) {
            String lower = filter.tickerSymbol().toLowerCase();
            data = data.stream().filter(s -> s.security().tickerSymbol() != null && s.security().tickerSymbol().value().toLowerCase().contains(lower)).toList();
        }
        if (filter.name() != null && !filter.name().isBlank()) {
            String lower = filter.name().toLowerCase();
            data = data.stream().filter(s -> s.security().name() != null && s.security().name().toLowerCase().contains(lower)).toList();
        }
        Set<String> countries = splitMultiValue(filter.country());
        if (!countries.isEmpty()) {
            data = data.stream().filter(s -> countries.contains(s.classification().country())).toList();
        }
        Set<String> branches = splitMultiValue(filter.branch());
        if (!branches.isEmpty()) {
            data = data.stream().filter(s -> branches.contains(s.classification().branch())).toList();
        }
        if (sort.sortField() != null && !sort.sortField().isBlank()) {
            data = SortHelper.sort(data, sort, SORT_FIELDS);
        }
        return data;
    }
}