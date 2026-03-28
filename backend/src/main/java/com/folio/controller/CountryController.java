package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.model.Country;
import com.folio.repository.CountryRepository;
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
@RequestMapping("/api/countries")
@Tag(name = "Countries", description = "Country reference data")
public class CountryController {

    private static final Map<String, Comparator<Country>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(Country::getName)
    );

    private final CountryRepository countryRepo;
    private final ExportService exportService;

    public CountryController(CountryRepository countryRepo, ExportService exportService) {
        this.countryRepo = countryRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all countries with sorting and pagination")
    public ResponseEntity<PaginatedResponseDto<Country>> getCountries(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<Country> data = sorted(sortField, sortDir);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export countries as CSV or Excel")
    public ResponseEntity<byte[]> exportCountries(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Country> data = sorted(sortField != null ? sortField : "name", sortDir);
        List<ExportColumn<Country>> columns = List.of(new ExportColumn<>("Country", Country::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "countries"));
    }

    private List<Country> sorted(String sortField, String sortDir) {
        List<Country> data = countryRepo.findAllByOrderByNameAsc();
        return SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
    }
}