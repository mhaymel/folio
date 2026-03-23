package com.folio.controller;

import com.folio.dto.TransactionDto;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction data endpoints")
public class TransactionController {

    private final PortfolioService portfolioService;

    public TransactionController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
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
}