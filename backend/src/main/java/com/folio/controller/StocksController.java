package com.folio.controller;

import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.StockDto;
import com.folio.dto.StockFiltersDto;
import com.folio.dto.StockPaginatedResponseDto;
import com.folio.service.PortfolioService;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
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
        Map.entry("isin", SortHelper.text(stock -> stock.security().isin() == null ? null : stock.security().isin().value())),
        Map.entry("tickerSymbol", SortHelper.text(stock -> stock.security().tickerSymbol() == null ? null : stock.security().tickerSymbol().value())),
        Map.entry("name", SortHelper.text(stock -> stock.security().name())),
        Map.entry("country", SortHelper.text(stock -> stock.classification().country())),
        Map.entry("branch", SortHelper.text(stock -> stock.classification().branch())),
        Map.entry("count", SortHelper.number(stock -> stock.metrics().position().count())),
        Map.entry("avgEntryPrice", SortHelper.number(stock -> stock.metrics().position().avgEntryPrice())),
        Map.entry("currentQuote", SortHelper.number(stock -> stock.metrics().position().currentQuote())),
        Map.entry("performancePercent", SortHelper.number(stock -> stock.metrics().performance().performancePercent())),
        Map.entry("dividendPerShare", SortHelper.number(stock -> stock.metrics().performance().dividendPerShare())),
        Map.entry("estimatedAnnualIncome", SortHelper.number(stock -> stock.metrics().performance().estimatedAnnualIncome()))
    );

    private final PortfolioService portfolioService;
    private final ListOperations listOperations;

    public StocksController(PortfolioService portfolioService, ListOperations listOperations) {
        this.portfolioService = requireNonNull(portfolioService);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get current portfolio positions aggregated across all depots")
    public ResponseEntity<StockPaginatedResponseDto> getStocks(
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String country,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<StockDto> data = filterAndSort(portfolioService.getAggregatedStocks(),
            new StockFilter(isin, tickerSymbol, name, "", country, branch),
            new SortRequest(sortField, sortDir));
        double sumCount = data.stream().mapToDouble(stock -> stock.metrics().position().count()).sum();
        PaginatedResponseDto<StockDto> paginated = listOperations.paginationHelper().paginate(data, page, pageSize);
        return ResponseEntity.ok(new StockPaginatedResponseDto(
            paginated.getItems(), paginated.getPage(), paginated.getPageSize(),
            paginated.getTotalItems(), paginated.getTotalPages(), sumCount));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for aggregated stocks")
    public ResponseEntity<StockFiltersDto> getStockFilters() {
        List<StockDto> stocks = portfolioService.getAggregatedStocks();
        List<String> countries = stocks.stream()
            .map(stock -> stock.classification().country()).filter(country -> country != null && !country.isBlank())
            .distinct().sorted().toList();
        List<String> branches = stocks.stream()
            .map(stock -> stock.classification().branch()).filter(branch -> branch != null && !branch.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(new StockFiltersDto(countries, branches, List.of()));
    }

    @GetMapping("/export")
    @Operation(summary = "Export aggregated stocks as CSV or Excel")
    public ResponseEntity<byte[]> exportStocks(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String country,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<StockDto> data = filterAndSort(portfolioService.getAggregatedStocks(),
            new StockFilter(isin, tickerSymbol, name, "", country, branch),
            new SortRequest(sortField, sortDir));

        List<ExportColumn<StockDto>> columns = List.of(
                new ExportColumn<>("ISIN", stock -> stock.security().isin()),
                new ExportColumn<>("Ticker", stock -> stock.security().tickerSymbol() == null ? null : stock.security().tickerSymbol().value()),
                new ExportColumn<>("Name", stock -> stock.security().name()),
                new ExportColumn<>("CountryEntity", stock -> stock.classification().country()),
                new ExportColumn<>("BranchEntity", stock -> stock.classification().branch()),
                new ExportColumn<>("Count", stock -> stock.metrics().position().count()),
                new ExportColumn<>("Avg Price", stock -> stock.metrics().position().avgEntryPrice()),
                new ExportColumn<>("Quote", stock -> stock.metrics().position().currentQuote()),
                new ExportColumn<>("Perf %", stock -> stock.metrics().performance().performancePercent()),
                new ExportColumn<>("Div/Share", stock -> stock.metrics().performance().dividendPerShare()),
                new ExportColumn<>("Est. Income", stock -> stock.metrics().performance().estimatedAnnualIncome())
        );

        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "stocks"));
    }

    private Set<String> splitMultiValue(String param) {
        if (param == null || param.isBlank()) return Set.of();
        return Arrays.stream(param.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toSet());
    }

    private List<StockDto> filterAndSort(List<StockDto> data, StockFilter filter,
                                                 SortRequest sort) {
        if (!filter.isinFragment().isBlank()) {
            String lower = filter.isinFragment().toLowerCase();
            data = data.stream().filter(stock -> stock.security().isin() != null && stock.security().isin().value().toLowerCase().contains(lower)).toList();
        }
        if (!filter.tickerSymbolFragment().isBlank()) {
            String lower = filter.tickerSymbolFragment().toLowerCase();
            data = data.stream().filter(stock -> stock.security().tickerSymbol() != null && stock.security().tickerSymbol().value().toLowerCase().contains(lower)).toList();
        }
        if (!filter.nameFragment().isBlank()) {
            String lower = filter.nameFragment().toLowerCase();
            data = data.stream().filter(stock -> stock.security().name() != null && stock.security().name().toLowerCase().contains(lower)).toList();
        }
        Set<String> countries = splitMultiValue(filter.countryFragment());
        if (!countries.isEmpty()) {
            data = data.stream().filter(stock -> countries.contains(stock.classification().country())).toList();
        }
        Set<String> branches = splitMultiValue(filter.branchFragment());
        if (!branches.isEmpty()) {
            data = data.stream().filter(stock -> branches.contains(stock.classification().branch())).toList();
        }
        if (sort.sortField() != null && !sort.sortField().isBlank()) {
            data = listOperations.sortHelper().sort(data, sort, SORT_FIELDS);
        }
        return data;
    }
}