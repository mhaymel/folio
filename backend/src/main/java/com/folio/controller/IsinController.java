package com.folio.controller;

import com.folio.domain.Isin;
import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.IsinDto;
import com.folio.dto.PaginatedResponseDto;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/isins")
@Tag(name = "ISINs", description = "All ISINs known to the system")
public class IsinController {

    private static final Map<String, Comparator<IsinDto>> SORT_FIELDS = Map.of(
        "isin", SortHelper.text(d -> d.getIsin() == null ? null : d.getIsin().value()),
        "tickerSymbol", SortHelper.text(IsinDto::getTickerSymbol),
        "name", SortHelper.text(IsinDto::getName),
        "country", SortHelper.text(IsinDto::getCountry),
        "branch", SortHelper.text(IsinDto::getBranch)
    );

    private final EntityManager em;
    private final ExportService exportService;

    public IsinController(EntityManager em, ExportService exportService) {
        this.em = requireNonNull(em);
        this.exportService = requireNonNull(exportService);
    }

    @GetMapping
    @Operation(summary = "Get all ISINs with associated metadata")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<IsinDto>> getIsins(
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String tickerSymbol,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<IsinDto> data = filterAndSort(loadAll(),
            new StockFilter(isin, tickerSymbol, name, null, country, branch),
            new SortRequest(sortField, sortDir));
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get distinct filter options for ISINs")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, List<String>>> getFilters() {
        List<IsinDto> all = loadAll();
        List<String> countries = all.stream()
            .map(IsinDto::getCountry).filter(c -> c != null && !c.isBlank())
            .distinct().sorted().toList();
        List<String> branches = all.stream()
            .map(IsinDto::getBranch).filter(b -> b != null && !b.isBlank())
            .distinct().sorted().toList();
        return ResponseEntity.ok(Map.of("countries", countries, "branches", branches));
    }

    @GetMapping("/export")
    @Operation(summary = "Export ISINs as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportIsins(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String tickerSymbol,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<IsinDto> data = filterAndSort(loadAll(),
            new StockFilter(isin, tickerSymbol, name, null, country, branch),
            new SortRequest(sortField, sortDir));
        List<ExportColumn<IsinDto>> columns = List.of(
            new ExportColumn<>("ISIN", IsinDto::getIsin),
            new ExportColumn<>("Ticker Symbol", IsinDto::getTickerSymbol),
            new ExportColumn<>("Name", IsinDto::getName),
            new ExportColumn<>("Country", IsinDto::getCountry),
            new ExportColumn<>("Branch", IsinDto::getBranch)
        );
        return exportService.export(new ExportRequest<>(data, columns, format, "isins"));
    }

    @SuppressWarnings("unchecked")
    private List<IsinDto> loadAll() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin,
                   (SELECT ts.symbol FROM isin_ticker it
                    JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
                    WHERE it.isin_id = i.id ORDER BY it.ticker_symbol_id ASC LIMIT 1),
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1),
                   (SELECT c.name FROM isin_country ic JOIN country c ON c.id = ic.country_id WHERE ic.isin_id = i.id LIMIT 1),
                   (SELECT b.name FROM isin_branch ib JOIN branch b ON b.id = ib.branch_id WHERE ib.isin_id = i.id LIMIT 1)
            FROM isin i
            ORDER BY i.isin ASC
            """).getResultList();

        return rows.stream()
            .map(r -> new IsinDto(
                new Isin((String) r[0]),
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4]))
            .toList();
    }

    private static Set<String> splitMultiValue(String param) {
        if (param == null || param.isBlank()) return Set.of();
        return Arrays.stream(param.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    private static List<IsinDto> filterAndSort(List<IsinDto> data, StockFilter filter,
                                                SortRequest sort) {
        if (filter.isin() != null && !filter.isin().isBlank()) {
            String lower = filter.isin().toLowerCase();
            data = data.stream().filter(d -> d.getIsin() != null && d.getIsin().value().toLowerCase().contains(lower)).toList();
        }
        if (filter.tickerSymbol() != null && !filter.tickerSymbol().isBlank()) {
            String lower = filter.tickerSymbol().toLowerCase();
            data = data.stream().filter(d -> d.getTickerSymbol() != null && d.getTickerSymbol().toLowerCase().contains(lower)).toList();
        }
        if (filter.name() != null && !filter.name().isBlank()) {
            String lower = filter.name().toLowerCase();
            data = data.stream().filter(d -> d.getName() != null && d.getName().toLowerCase().contains(lower)).toList();
        }
        Set<String> countries = splitMultiValue(filter.country());
        if (!countries.isEmpty()) {
            data = data.stream().filter(d -> countries.contains(d.getCountry())).toList();
        }
        Set<String> branches = splitMultiValue(filter.branch());
        if (!branches.isEmpty()) {
            data = data.stream().filter(d -> branches.contains(d.getBranch())).toList();
        }
        if (sort.sortField() != null && !sort.sortField().isBlank()) {
            data = SortHelper.sort(data, sort, SORT_FIELDS);
        }
        return data;
    }
}
