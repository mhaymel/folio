package com.folio.controller;

import com.folio.dto.TransactionDto;
import com.folio.service.ExportService;
import com.folio.service.ExportService.Column;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction data endpoints")
public class TransactionController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PortfolioService portfolioService;
    private final ExportService exportService;

    public TransactionController(PortfolioService portfolioService, ExportService exportService) {
        this.portfolioService = portfolioService;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all transactions with optional filters")
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String depot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(portfolioService.getTransactions(isin, depot, fromDate, toDate));
    }

    @GetMapping("/export")
    @Operation(summary = "Export transactions as CSV or Excel")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String depot,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<TransactionDto> data = portfolioService.getTransactions(null, null, null, null);

        // Client-side-style partial filters (matching frontend behaviour)
        if (isin != null && !isin.isBlank()) {
            String lower = isin.toLowerCase();
            data = data.stream().filter(t -> t.getIsin() != null && t.getIsin().toLowerCase().contains(lower)).toList();
        }
        if (name != null && !name.isBlank()) {
            String lower = name.toLowerCase();
            data = data.stream().filter(t -> t.getName() != null && t.getName().toLowerCase().contains(lower)).toList();
        }
        if (depot != null && !depot.isBlank()) {
            data = data.stream().filter(t -> depot.equals(t.getDepot())).toList();
        }
        if (sortField != null && !sortField.isBlank()) {
            data = sorted(data, sortField, sortDir);
        }

        List<Column<TransactionDto>> columns = List.of(
                new Column<>("Date", t -> t.getDate() != null ? t.getDate().format(DATE_FMT) : ""),
                new Column<>("ISIN", TransactionDto::getIsin),
                new Column<>("Name", TransactionDto::getName),
                new Column<>("Depot", TransactionDto::getDepot),
                new Column<>("Count", TransactionDto::getCount),
                new Column<>("Share Price", TransactionDto::getSharePrice)
        );

        return exportService.export(data, columns, format, "transactions");
    }

    private static List<TransactionDto> sorted(List<TransactionDto> data, String field, String dir) {
        Comparator<TransactionDto> cmp = switch (field) {
            case "date" -> Comparator.comparing(TransactionDto::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "isin" -> Comparator.comparing(TransactionDto::getIsin, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(TransactionDto::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "depot" -> Comparator.comparing(TransactionDto::getDepot, Comparator.nullsLast(String::compareToIgnoreCase));
            case "count" -> Comparator.comparing(TransactionDto::getCount, Comparator.nullsLast(Double::compareTo));
            case "sharePrice" -> Comparator.comparing(TransactionDto::getSharePrice, Comparator.nullsLast(Double::compareTo));
            default -> null;
        };
        if (cmp == null) return data;
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return data.stream().sorted(cmp).toList();
    }
}