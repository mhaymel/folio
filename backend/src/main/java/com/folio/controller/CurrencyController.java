package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.model.Currency;
import com.folio.repository.CurrencyRepository;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@Tag(name = "Currencies", description = "Currency reference data")
public class CurrencyController {

    private final CurrencyRepository currencyRepo;
    private final ExportService exportService;

    public CurrencyController(CurrencyRepository currencyRepo, ExportService exportService) {
        this.currencyRepo = currencyRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all currencies sorted alphabetically")
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(currencyRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/export")
    @Operation(summary = "Export currencies as CSV or Excel")
    public ResponseEntity<byte[]> exportCurrencies(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Currency> data = currencyRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Currency::getName).reversed()).toList();
        }
        List<ExportColumn<Currency>> columns = List.of(new ExportColumn<>("Currency", Currency::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "currencies"));
    }
}

