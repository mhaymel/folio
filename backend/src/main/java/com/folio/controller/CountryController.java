package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.model.Country;
import com.folio.repository.CountryRepository;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/countries")
@Tag(name = "Countries", description = "Country reference data")
public class CountryController {

    private final CountryRepository countryRepo;
    private final ExportService exportService;

    public CountryController(CountryRepository countryRepo, ExportService exportService) {
        this.countryRepo = countryRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all countries sorted alphabetically")
    public ResponseEntity<List<Country>> getCountries() {
        return ResponseEntity.ok(countryRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/export")
    @Operation(summary = "Export countries as CSV or Excel")
    public ResponseEntity<byte[]> exportCountries(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Country> data = countryRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Country::getName).reversed()).toList();
        }
        List<ExportColumn<Country>> columns = List.of(new ExportColumn<>("Country", Country::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "countries"));
    }
}

