package com.folio.controller;

import com.folio.dto.ImportResult;
import com.folio.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Import", description = "CSV file import endpoints")
public final class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/degiro/transactions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import DeGiro Transactions.csv")
    public ResponseEntity<ImportResult> importDegiroTransactions(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importDegiroTransactions(file));
    }

    @PostMapping(value = "/degiro/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import DeGiro Account.csv (dividends)")
    public ResponseEntity<ImportResult> importDegiroAccount(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importDegiroAccount(file));
    }

    @PostMapping(value = "/zero/orders", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import ZERO orders CSV")
    public ResponseEntity<ImportResult> importZeroOrders(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importZeroOrders(file));
    }

    @PostMapping(value = "/zero/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import ZERO kontoumsaetze CSV (dividends)")
    public ResponseEntity<ImportResult> importZeroAccount(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importZeroAccount(file));
    }

    @PostMapping(value = "/dividends", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import dividende.csv")
    public ResponseEntity<ImportResult> importDividends(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importDividends(file));
    }

    @PostMapping(value = "/branches", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import branches.csv")
    public ResponseEntity<ImportResult> importBranches(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importBranches(file));
    }

    @PostMapping(value = "/countries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import countries.csv")
    public ResponseEntity<ImportResult> importCountries(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importCountries(file));
    }

    @PostMapping(value = "/ticker-symbols", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import ticker_symbol.csv")
    public ResponseEntity<ImportResult> importTickerSymbols(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importTickerSymbols(file));
    }
}