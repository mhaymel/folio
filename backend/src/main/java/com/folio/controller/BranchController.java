package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.model.Branch;
import com.folio.repository.BranchRepository;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
@Tag(name = "Branches", description = "Branch reference data")
public class BranchController {

    private final BranchRepository branchRepo;
    private final ExportService exportService;

    public BranchController(BranchRepository branchRepo, ExportService exportService) {
        this.branchRepo = branchRepo;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all branches sorted alphabetically")
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(branchRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/export")
    @Operation(summary = "Export branches as CSV or Excel")
    public ResponseEntity<byte[]> exportBranches(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Branch> data = branchRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Branch::getName).reversed()).toList();
        }
        List<ExportColumn<Branch>> columns = List.of(new ExportColumn<>("Branch", Branch::getName));
        return exportService.export(new ExportRequest<>(data, columns, format, "branches"));
    }
}

