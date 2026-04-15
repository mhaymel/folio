package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.model.CountryEntity;
import com.folio.repository.CountryRepository;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/countries")
@Tag(name = "Countries", description = "CountryEntity reference data")
final class CountryController {

    private static final Map<String, Comparator<CountryEntity>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(CountryEntity::getName)
    );

    private final CountryRepository countryRepository;
    private final ListOperations listOperations;

    public CountryController(CountryRepository countryRepository, ListOperations listOperations) {
        this.countryRepository = requireNonNull(countryRepository);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get all countries with sorting and pagination")
    public ResponseEntity<PaginatedResponseDto<CountryEntity>> getCountries(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<CountryEntity> data = sorted(sortField, sortDir);
        return ResponseEntity.ok(listOperations.paginationHelper().paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export countries as CSV or Excel")
    public ResponseEntity<byte[]> exportCountries(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<CountryEntity> data = sorted(sortField != null ? sortField : "name", sortDir);
        List<ExportColumn<CountryEntity>> columns = List.of(new ExportColumn<>("CountryEntity", CountryEntity::getName));
        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "countries"));
    }

    private List<CountryEntity> sorted(String sortField, String sortDir) {
        List<CountryEntity> data = countryRepository.findAllByOrderByNameAsc();
        return listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);
    }
}