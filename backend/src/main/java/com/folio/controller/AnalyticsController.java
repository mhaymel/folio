package com.folio.controller;

import com.folio.dto.DiversificationDto;
import com.folio.dto.DiversificationEntry;
import com.folio.dto.ExportRequest;
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
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Portfolio diversification analytics")
public class AnalyticsController {

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public AnalyticsController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
    }

    @GetMapping("/countries")
    @Operation(summary = "Country diversification breakdown")
    public ResponseEntity<DiversificationDto> getCountryDiversification() {
        return ResponseEntity.ok(portfolioService.getCountryDiversification());
    }

    @GetMapping("/branches")
    @Operation(summary = "Branch diversification breakdown")
    public ResponseEntity<DiversificationDto> getBranchDiversification() {
        return ResponseEntity.ok(portfolioService.getBranchDiversification());
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
            data = sortEntries(data, sortField, sortDir);
        }

        String label = "countries".equals(type) ? "Country" : "Branch";

        List<ExportColumn<DiversificationEntry>> columns = List.of(
                new ExportColumn<>(label, DiversificationEntry::getName),
                new ExportColumn<>("Invested (EUR)", DiversificationEntry::getInvestedAmount),
                new ExportColumn<>("%", DiversificationEntry::getPercentage)
        );

        return exportService.export(new ExportRequest<>(data, columns, format, type + "-diversification"));
    }

    private static List<DiversificationEntry> sortEntries(List<DiversificationEntry> data, String field, String dir) {
        Comparator<DiversificationEntry> cmp = switch (field) {
            case "name" -> Comparator.comparing(DiversificationEntry::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "investedAmount" -> Comparator.comparing(DiversificationEntry::getInvestedAmount, Comparator.nullsLast(Double::compareTo));
            case "percentage" -> Comparator.comparing(DiversificationEntry::getPercentage, Comparator.nullsLast(Double::compareTo));
            default -> null;
        };
        if (cmp == null) return data;
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return data.stream().sorted(cmp).toList();
    }
}