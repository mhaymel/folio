package com.folio.controller;

import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.TransactionDto;
import com.folio.dto.TransactionFilter;
import com.folio.dto.TransactionFiltersDto;
import com.folio.dto.TransactionPaginatedResponseDto;
import com.folio.service.PortfolioService;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "TransactionEntity data endpoints")
final class TransactionController {

    private static final Map<String, Comparator<TransactionDto>> SORT_FIELDS = Map.of(
        "date", SortHelper.comparing(TransactionDto::getRawDate),
        "isin", SortHelper.text(transaction -> transaction.getIsin() == null ? null : transaction.getIsin().value()),
        "tickerSymbol", SortHelper.text(TransactionDto::getTickerSymbol),
        "name", SortHelper.text(TransactionDto::getName),
        "depot", SortHelper.text(TransactionDto::getDepot),
        "count", SortHelper.number(TransactionDto::getCount),
        "sharePrice", SortHelper.number(TransactionDto::getSharePrice)
    );

    private final PortfolioService portfolioService;
    private final ListOperations listOperations;

    public TransactionController(PortfolioService portfolioService, ListOperations listOperations) {
        this.portfolioService = requireNonNull(portfolioService);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get all transactions with optional filters, sorting, and pagination")
    public ResponseEntity<TransactionPaginatedResponseDto> getTransactions(
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String depot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false, defaultValue = "date") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        List<TransactionDto> data = portfolioService.getTransactions(
            new TransactionFilter(isin, tickerSymbol, name, depot, fromDate, toDate));

        data = listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);

        long filteredCount = data.size();
        double sumCount = data.stream().mapToDouble(transaction -> transaction.getCount() != null ? transaction.getCount() : 0).sum();

        PaginatedResponseDto<TransactionDto> paginated = listOperations.paginationHelper().paginate(data, page, pageSize);

        return ResponseEntity.ok(new TransactionPaginatedResponseDto(
            paginated.getItems(), paginated.getPage(), paginated.getPageSize(),
            paginated.getTotalItems(), paginated.getTotalPages(),
            filteredCount, sumCount
        ));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for transactions")
    public ResponseEntity<TransactionFiltersDto> getTransactionFilters() {
        List<TransactionDto> allTransactions = portfolioService.getTransactions(TransactionFilter.none());
        List<String> depots = allTransactions.stream()
            .map(TransactionDto::getDepot).filter(depot -> depot != null && !depot.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(new TransactionFiltersDto(depots));
    }

    @GetMapping("/export")
    @Operation(summary = "Export transactions as CSV or Excel")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String depot,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<TransactionDto> data = portfolioService.getTransactions(
            new TransactionFilter(isin, tickerSymbol, name, depot, null, null));

        if (sortField != null && !sortField.isBlank()) {
            data = listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);
        }

        List<ExportColumn<TransactionDto>> columns = List.of(
                new ExportColumn<>("Date", TransactionDto::getDate),
                new ExportColumn<>("ISIN", TransactionDto::getIsin),
                new ExportColumn<>("Ticker", TransactionDto::getTickerSymbol),
                new ExportColumn<>("Name", TransactionDto::getName),
                new ExportColumn<>("DepotEntity", TransactionDto::getDepot),
                new ExportColumn<>("Count", TransactionDto::getCount),
                new ExportColumn<>("Share Price", TransactionDto::getSharePrice)
        );

        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "transactions"));
    }
}