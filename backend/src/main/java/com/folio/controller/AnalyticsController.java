package com.folio.controller;

import com.folio.dto.DiversificationDto;
import com.folio.dto.DiversificationEntry;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.dto.ExportColumn;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Portfolio diversification analytics")
public final class AnalyticsController {

    private static final Map<String, Comparator<DiversificationEntry>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(DiversificationEntry::getName),
        "investedAmount", SortHelper.number(DiversificationEntry::getInvestedAmount),
        "percentage", SortHelper.number(DiversificationEntry::getPercentage)
    );

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public AnalyticsController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
    }

    @GetMapping("/countries")
    @Operation(summary = "CountryEntity diversification breakdown")
    public ResponseEntity<PaginatedResponseDto<DiversificationEntry>> getCountryDiversification(
            @RequestParam(required = false, defaultValue = "investedAmount") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<DiversificationEntry> entries = portfolioService.getCountryDiversification().getEntries();
        entries = SortHelper.sort(entries, sortField, sortDir, SORT_FIELDS);
        return ResponseEntity.ok(PaginationHelper.paginate(entries, page, pageSize));
    }

    @GetMapping("/branches")
    @Operation(summary = "BranchEntity diversification breakdown")
    public ResponseEntity<PaginatedResponseDto<DiversificationEntry>> getBranchDiversification(
            @RequestParam(required = false, defaultValue = "investedAmount") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<DiversificationEntry> entries = portfolioService.getBranchDiversification().getEntries();
        entries = SortHelper.sort(entries, sortField, sortDir, SORT_FIELDS);
        return ResponseEntity.ok(PaginationHelper.paginate(entries, page, pageSize));
    }

    @GetMapping("/{type}/export")
    @Operation(summary = "Export diversification data as CSV or Excel")
    public ResponseEntity<byte[]> exportDiversification(
            @PathVariable String type,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {

        DiversificationDto dto = "countries".equals(type)
                ? portfolioService.getCountryDiversification()
                : portfolioService.getBranchDiversification();

        List<DiversificationEntry> data = dto.getEntries();
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
        }

        String label = "countries".equals(type) ? "CountryEntity" : "BranchEntity";

        List<ExportColumn<DiversificationEntry>> columns = List.of(
                new ExportColumn<>(label, DiversificationEntry::getName),
                new ExportColumn<>("Invested (EUR)", DiversificationEntry::getInvestedAmount),
                new ExportColumn<>("%", DiversificationEntry::getPercentage)
        );

        return exportService.export(new ExportRequest<>(data, columns, format, type + "-diversification"));
    }
}