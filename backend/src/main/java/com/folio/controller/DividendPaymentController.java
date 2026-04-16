package com.folio.controller;

import com.folio.dto.DividendPaymentDto;
import com.folio.dto.DividendPaymentFilter;
import com.folio.dto.DividendPaymentFiltersDto;
import com.folio.dto.DividendPaymentPaginatedResponseDto;
import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.service.DividendPaymentService;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/dividend-payments")
@Tag(name = "DividendEntity Payments", description = "DividendEntity payment endpoints")
final class DividendPaymentController {

    private static final Map<String, Comparator<DividendPaymentDto>> SORT_FIELDS = Map.of(
        "timestamp", SortHelper.comparing(DividendPaymentDto::getRawTimestamp),
        "isin", SortHelper.text(dto -> dto.getIsin() == null ? null : dto.getIsin().value()),
        "name", SortHelper.text(DividendPaymentDto::getName),
        "depot", SortHelper.text(DividendPaymentDto::getDepot),
        "value", SortHelper.number(DividendPaymentDto::getValue)
    );

    private final DividendPaymentService dividendPaymentService;
    private final ListOperations listOperations;

    DividendPaymentController(DividendPaymentService dividendPaymentService, ListOperations listOperations) {
        this.dividendPaymentService = requireNonNull(dividendPaymentService);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get all dividend payments with optional filters, sorting, and pagination")
    public ResponseEntity<DividendPaymentPaginatedResponseDto> getDividendPayments(
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String depot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "timestamp") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        List<DividendPaymentDto> data = dividendPaymentService.getDividendPayments(
            new DividendPaymentFilter(isin, name, depot, fromDate, toDate));

        data = listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);

        double sumValue = data.stream().mapToDouble(payment -> payment.getValue() != null ? payment.getValue() : 0).sum();

        PaginatedResponseDto<DividendPaymentDto> paginated = listOperations.paginationHelper().paginate(data, page, pageSize);

        return ResponseEntity.ok(new DividendPaymentPaginatedResponseDto(
            paginated.getItems(), paginated.getPage(), paginated.getPageSize(),
            paginated.getTotalItems(), paginated.getTotalPages(), sumValue
        ));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for dividend payments")
    public ResponseEntity<DividendPaymentFiltersDto> getFilters() {
        List<DividendPaymentDto> all = dividendPaymentService.getDividendPayments(DividendPaymentFilter.none());
        List<String> depots = all.stream()
            .map(DividendPaymentDto::getDepot).filter(depot -> depot != null && !depot.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(new DividendPaymentFiltersDto(depots));
    }

    @GetMapping("/export")
    @Operation(summary = "Export dividend payments as CSV or Excel")
    public ResponseEntity<byte[]> exportDividendPayments(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String depot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<DividendPaymentDto> data = dividendPaymentService.getDividendPayments(
            new DividendPaymentFilter(isin, name, depot, fromDate, toDate));

        if (sortField != null && !sortField.isBlank()) {
            data = listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);
        }

        List<ExportColumn<DividendPaymentDto>> columns = List.of(
                new ExportColumn<>("Date", DividendPaymentDto::getTimestamp),
                new ExportColumn<>("ISIN", DividendPaymentDto::getIsin),
                new ExportColumn<>("Name", DividendPaymentDto::getName),
                new ExportColumn<>("DepotEntity", DividendPaymentDto::getDepot),
                new ExportColumn<>("Value (EUR)", DividendPaymentDto::getValue)
        );

        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "dividend-payments"));
    }
}