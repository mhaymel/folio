package com.folio.controller;

import com.folio.model.Branch;
import com.folio.model.Country;
import com.folio.model.Currency;
import com.folio.model.Depot;
import com.folio.repository.BranchRepository;
import com.folio.repository.CountryRepository;
import com.folio.repository.CurrencyRepository;
import com.folio.repository.DepotRepository;
import com.folio.service.ExportService;
import com.folio.service.ExportService.Column;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reference Data", description = "Countries, branches, depots, currencies")
public class ReferenceDataController {

    private final CountryRepository countryRepo;
    private final BranchRepository branchRepo;
    private final DepotRepository depotRepo;
    private final CurrencyRepository currencyRepo;
    private final ExportService exportService;

    public ReferenceDataController(CountryRepository countryRepo, BranchRepository branchRepo,
                                    DepotRepository depotRepo, CurrencyRepository currencyRepo,
                                    ExportService exportService) {
        this.countryRepo = countryRepo;
        this.branchRepo = branchRepo;
        this.depotRepo = depotRepo;
        this.currencyRepo = currencyRepo;
        this.exportService = exportService;
    }

    // ── Countries ───────────────────────────────────────────────────────────

    @GetMapping("/countries")
    @Operation(summary = "Get all countries sorted alphabetically")
    public ResponseEntity<List<Country>> getCountries() {
        return ResponseEntity.ok(countryRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/countries/export")
    @Operation(summary = "Export countries as CSV or Excel")
    public ResponseEntity<byte[]> exportCountries(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Country> data = countryRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Country::getName).reversed()).toList();
        }
        List<Column<Country>> columns = List.of(new Column<>("Country", Country::getName));
        return exportService.export(data, columns, format, "countries");
    }

    // ── Branches ────────────────────────────────────────────────────────────

    @GetMapping("/branches")
    @Operation(summary = "Get all branches sorted alphabetically")
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(branchRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/branches/export")
    @Operation(summary = "Export branches as CSV or Excel")
    public ResponseEntity<byte[]> exportBranches(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Branch> data = branchRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Branch::getName).reversed()).toList();
        }
        List<Column<Branch>> columns = List.of(new Column<>("Branch", Branch::getName));
        return exportService.export(data, columns, format, "branches");
    }

    // ── Depots ──────────────────────────────────────────────────────────────

    @GetMapping("/depots")
    @Operation(summary = "Get all depots sorted alphabetically")
    public ResponseEntity<List<Depot>> getDepots() {
        return ResponseEntity.ok(depotRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/depots/export")
    @Operation(summary = "Export depots as CSV or Excel")
    public ResponseEntity<byte[]> exportDepots(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Depot> data = depotRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Depot::getName).reversed()).toList();
        }
        List<Column<Depot>> columns = List.of(new Column<>("Depot", Depot::getName));
        return exportService.export(data, columns, format, "depots");
    }

    // ── Currencies ──────────────────────────────────────────────────────────

    @GetMapping("/currencies")
    @Operation(summary = "Get all currencies sorted alphabetically")
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(currencyRepo.findAllByOrderByNameAsc());
    }

    @GetMapping("/currencies/export")
    @Operation(summary = "Export currencies as CSV or Excel")
    public ResponseEntity<byte[]> exportCurrencies(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<Currency> data = currencyRepo.findAllByOrderByNameAsc();
        if ("name".equals(sortField) && "desc".equalsIgnoreCase(sortDir)) {
            data = data.stream().sorted(Comparator.comparing(Currency::getName).reversed()).toList();
        }
        List<Column<Currency>> columns = List.of(new Column<>("Currency", Currency::getName));
        return exportService.export(data, columns, format, "currencies");
    }
}