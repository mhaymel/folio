package com.folio.controller;

import com.folio.dto.IsinNameDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/isin-names")
@Tag(name = "ISIN Names", description = "ISIN to security name mappings")
public class IsinNameController {

    private final EntityManager em;

    public IsinNameController(EntityManager em) {
        this.em = em;
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to name mappings")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<IsinNameDto>> getIsinNames() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin, n.name
            FROM isin_name n
            JOIN isin i ON i.id = n.isin_id
            ORDER BY n.name ASC, i.isin ASC
            """).getResultList();

        List<IsinNameDto> result = rows.stream()
            .map(r -> IsinNameDto.builder()
                .isin((String) r[0])
                .name((String) r[1])
                .build())
            .toList();

        return ResponseEntity.ok(result);
    }
}

