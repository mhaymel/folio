package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.model.BranchEntity;
import com.folio.repository.BranchRepository;
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
@RequestMapping("/api/branches")
@Tag(name = "Branches", description = "BranchEntity reference data")
final class BranchController {

    private static final Map<String, Comparator<BranchEntity>> SORT_FIELDS = Map.of(
        "name", SortHelper.text(BranchEntity::getName)
    );

    private final BranchRepository branchRepository;
    private final ListOperations listOperations;

    BranchController(BranchRepository branchRepository, ListOperations listOperations) {
        this.branchRepository = requireNonNull(branchRepository);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get all branches with sorting and pagination")
    public ResponseEntity<PaginatedResponseDto<BranchEntity>> getBranches(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<BranchEntity> data = sorted(sortField, sortDir);
        return ResponseEntity.ok(listOperations.paginationHelper().paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export branches as CSV or Excel")
    public ResponseEntity<byte[]> exportBranches(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<BranchEntity> data = sorted(sortField != null ? sortField : "name", sortDir);
        List<ExportColumn<BranchEntity>> columns = List.of(new ExportColumn<>("BranchEntity", BranchEntity::getName));
        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "branches"));
    }

    private List<BranchEntity> sorted(String sortField, String sortDir) {
        List<BranchEntity> data = branchRepository.findAllByOrderByNameAsc();
        return listOperations.sortHelper().sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);
    }
}