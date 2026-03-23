package com.folio.controller;

import com.folio.dto.DashboardDto;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Portfolio dashboard summary")
public class DashboardController {

    private final PortfolioService portfolioService;

    public DashboardController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(portfolioService.getDashboard());
    }
}