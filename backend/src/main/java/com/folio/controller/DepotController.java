package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.model.Depot;
import com.folio.repository.DepotRepository;
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
@RequestMapping("/api/depots")
@Tag(name = "Depots", description = "Depot reference data")
public class DepotController {

    private static final Map<String, Comparator<Depot>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(Depot::getName)
    );

    private final DepotRepository depotRepo;
    private final ExportService exportService;

    public DepotController(DepotRepository depotRepo, ExportService exportService) {
        this.depotRepo = depotRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all depots with sorting and pagination")
    public ResponseEntity<PaginatedResponseDto<Depot>> getDepots(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<Depot> data = sorted(sortField, sortDir);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export depots as CSV or Excel")
    public ResponseEntity<byte[]> exportDepots(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Depot> data = sorted(sortField != null ? sortField : "name", sortDir);
        List<ExportColumn<Depot>> columns = List.of(new ExportColumn<>("Depot", Depot::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "depots"));
    }

    private List<Depot> sorted(String sortField, String sortDir) {
        List<Depot> data = depotRepo.findAllByOrderByNameAsc();
        return SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
    }
}