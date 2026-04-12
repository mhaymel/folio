package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.IsinNameDto;
import com.folio.dto.PaginatedResponseDto;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.folio.domain.Isin;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/isin-names")
@Tag(name = "ISIN Names", description = "ISIN to stock name mappings")
public class IsinNameController {

    private static final Map<String, Comparator<IsinNameDto>> SORT_FIELDS = Map.of(
        "isin", SortHelper.text(d -> d.getIsin() == null ? null : d.getIsin().value()),
        "name", SortHelper.text(IsinNameDto::getName)
    );

    private final EntityManager em;
    private final ExportService exportService;

    public IsinNameController(EntityManager em, ExportService exportService) {
        this.em = requireNonNull(em);
        this.exportService = requireNonNull(exportService);
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to name mappings")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<IsinNameDto>> getIsinNames(
            @RequestParam(required = false, defaultValue = "name") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<IsinNameDto> data = SortHelper.sort(loadAll(), new SortRequest(sortField, sortDir), SORT_FIELDS);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export ISIN names as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportIsinNames(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<IsinNameDto> data = loadAll();
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, new SortRequest(sortField, sortDir), SORT_FIELDS);
        }
        List<ExportColumn<IsinNameDto>> columns = List.of(
                new ExportColumn<>("ISIN", IsinNameDto::getIsin),
                new ExportColumn<>("Name", IsinNameDto::getName)
        );
        return exportService.export(new ExportRequest<>(data, columns, format, "isin-names"));
    }

    @SuppressWarnings("unchecked")
    private List<IsinNameDto> loadAll() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin, n.name
            FROM isin_name n
            JOIN isin i ON i.id = n.isin_id
            ORDER BY n.name ASC, i.isin ASC
            """).getResultList();

        return rows.stream()
            .map(r -> new IsinNameDto(new Isin((String) r[0]), (String) r[1]))
            .toList();
    }
}