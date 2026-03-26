package com.folio.controller;

import com.folio.dto.IsinNameDto;
import com.folio.service.ExportService;
import com.folio.service.ExportService.Column;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/isin-names")
@Tag(name = "ISIN Names", description = "ISIN to stock name mappings")
public class IsinNameController {

    private final EntityManager em;
    private final ExportService exportService;

    public IsinNameController(EntityManager em, ExportService exportService) {
        this.em = em;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to name mappings")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<IsinNameDto>> getIsinNames() {
        return ResponseEntity.ok(loadAll());
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
            data = sorted(data, sortField, sortDir);
        }
        List<Column<IsinNameDto>> columns = List.of(
                new Column<>("ISIN", IsinNameDto::getIsin),
                new Column<>("Name", IsinNameDto::getName)
        );
        return exportService.export(data, columns, format, "isin-names");
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
            .map(r -> IsinNameDto.builder()
                .isin((String) r[0])
                .name((String) r[1])
                .build())
            .toList();
    }

    private static List<IsinNameDto> sorted(List<IsinNameDto> data, String field, String dir) {
        Comparator<IsinNameDto> cmp = switch (field) {
            case "isin" -> Comparator.comparing(IsinNameDto::getIsin, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(IsinNameDto::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> null;
        };
        if (cmp == null) return data;
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return data.stream().sorted(cmp).toList();
    }
}
