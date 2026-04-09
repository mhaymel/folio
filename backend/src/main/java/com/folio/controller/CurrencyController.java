package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.model.CurrencyEntity;
import com.folio.repository.CurrencyRepository;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
@Tag(name = "Currencies", description = "CurrencyEntity reference data")
public final class CurrencyController {

    private static final Map<String, Comparator<CurrencyEntity>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(CurrencyEntity::getName)
    );

    private final CurrencyRepository currencyRepo;
    private final ExportService exportService;

    public CurrencyController(CurrencyRepository currencyRepo, ExportService exportService) {
        this.currencyRepo = currencyRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all currencies with sorting and pagination")
    public ResponseEntity<PaginatedResponseDto<CurrencyEntity>> getCurrencies(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<CurrencyEntity> data = sorted(sortField, sortDir);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export currencies as CSV or Excel")
    public ResponseEntity<byte[]> exportCurrencies(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<CurrencyEntity> data = sorted(sortField != null ? sortField : "name", sortDir);
        List<ExportColumn<CurrencyEntity>> columns = List.of(new ExportColumn<>("CurrencyEntity", CurrencyEntity::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "currencies"));
    }

    private List<CurrencyEntity> sorted(String sortField, String sortDir) {
        List<CurrencyEntity> data = currencyRepo.findAllByOrderByNameAsc();
        return SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
    }
}