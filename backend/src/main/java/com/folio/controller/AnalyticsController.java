package com.folio.controller;

import com.folio.dto.DiversificationDto;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Portfolio diversification analytics")
public class AnalyticsController {

    private final PortfolioService portfolioService;

    public AnalyticsController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/countries")
    @Operation(summary = "Country diversification breakdown")
    public ResponseEntity<DiversificationDto> getCountryDiversification() {
        return ResponseEntity.ok(portfolioService.getCountryDiversification());
    }

    @GetMapping("/branches")
    @Operation(summary = "Branch diversification breakdown")
    public ResponseEntity<DiversificationDto> getBranchDiversification() {
        return ResponseEntity.ok(portfolioService.getBranchDiversification());
    }
}