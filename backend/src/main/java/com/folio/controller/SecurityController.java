package com.folio.controller;

import com.folio.dto.SecurityDto;
import com.folio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/securities")
@Tag(name = "Securities", description = "Portfolio positions")
public class SecurityController {

    private final PortfolioService portfolioService;

    public SecurityController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "Get current portfolio positions")
    public ResponseEntity<List<SecurityDto>> getSecurities() {
        return ResponseEntity.ok(portfolioService.getSecurities());
    }
}