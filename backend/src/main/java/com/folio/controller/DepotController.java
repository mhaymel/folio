package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.model.Depot;
import com.folio.repository.DepotRepository;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/depots")
@Tag(name = "Depots", description = "Depot reference data")
public class DepotController {

    private final DepotRepository depotRepo;
    private final ExportService exportService;

    public DepotController(DepotRepository depotRepo, ExportService exportService) {
        this.depotRepo = depotRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all depots sorted alphabetically")
    public ResponseEntity<List<Depot>> getDepots() {
        return ResponseEntity.ok(depotRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/export")
    @Operation(summary = "Export depots as CSV or Excel")
    public ResponseEntity<byte[]> exportDepots(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Depot> data = depotRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Depot::getName).reversed()).toList();
        }
        List<ExportColumn<Depot>> columns = List.of(new ExportColumn<>("Depot", Depot::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "depots"));
    }
}

