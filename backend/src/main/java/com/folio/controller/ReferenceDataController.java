package com.folio.controller;

import com.folio.model.Branch;
import com.folio.model.Country;
import com.folio.model.Currency;
import com.folio.model.Depot;
import com.folio.repository.BranchRepository;
import com.folio.repository.CountryRepository;
import com.folio.repository.CurrencyRepository;
import com.folio.repository.DepotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reference Data", description = "Countries, branches, depots, currencies")
public class ReferenceDataController {

    private final CountryRepository countryRepo;
    private final BranchRepository branchRepo;
    private final DepotRepository depotRepo;
    private final CurrencyRepository currencyRepo;

    public ReferenceDataController(CountryRepository countryRepo, BranchRepository branchRepo, DepotRepository depotRepo, CurrencyRepository currencyRepo) {
        this.countryRepo = countryRepo;
        this.branchRepo = branchRepo;
        this.depotRepo = depotRepo;
        this.currencyRepo = currencyRepo;
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all countries sorted alphabetically")
    public ResponseEntity<List<Country>> getCountries() {
        return ResponseEntity.ok(countryRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/branches")
    @Operation(summary = "Get all branches sorted alphabetically")
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(branchRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/depots")
    @Operation(summary = "Get all depots sorted alphabetically")
    public ResponseEntity<List<Depot>> getDepots() {
        return ResponseEntity.ok(depotRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/currencies")
    @Operation(summary = "Get all currencies sorted alphabetically")
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(currencyRepo.findAllByOrderByNameAsc());
    }
}