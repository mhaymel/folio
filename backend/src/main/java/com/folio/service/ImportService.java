package com.folio.service;

import com.folio.dto.ImportResult;
import com.folio.model.*;
import com.folio.parser.*;
import com.folio.repository.ImportRepositories;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class ImportService {

    private static final Logger log = getLogger(ImportService.class);

    /**
     * Serialises all import operations so that concurrent HTTP requests do not
     * produce lock-timeout errors on the shared {@code isin} table.
     * Each {@code @Transactional} import method acquires this lock before
     * touching the database and releases it in a {@code finally} block.
     */
    private final ReentrantLock importLock = new ReentrantLock();

    private final ImportRepositories repos;
    private final EntityManager em;

    public ImportService(ImportRepositories repos, EntityManager em) {
        this.repos = repos;
        this.em = em;
    }

    // -- Formatting --

    private static String formatDuration(long ms) {
        if (ms >= 1000) {
            return (ms / 1000) + "s " + (ms % 1000) + "ms";
        }
        return ms + "ms";
    }

    // -- File reading --

    private static List<String> readLines(MultipartFile file) {
        long start = System.currentTimeMillis();
        try (InputStream is = file.getInputStream()) {
            List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().toList();
            log.info("Read {} lines in {} ms", lines.size(), System.currentTimeMillis() - start);
            return lines;
        } catch (Exception e) {
            log.warn("Failed to read file: {}", e.getMessage());
            return null;
        }
    }

    // -- Bulk DB helpers --

    private Map<String, Isin> bulkUpsertIsins(Set<String> isinCodes) {
        if (isinCodes.isEmpty()) return Map.of();

        Map<String, Isin> map = new HashMap<>();
        repos.isin().findByIsinIn(isinCodes).forEach(i -> map.put(i.getIsin(), i));

        Set<String> missing = new HashSet<>(isinCodes);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            List<Isin> created = repos.isin().saveAll(
                missing.stream().map(code -> Isin.builder().isin(code).build()).toList());
            created.forEach(i -> map.put(i.getIsin(), i));
        }
        return map;
    }

    private void bulkUpsertIsinNames(Map<String, Isin> isinMap, Map<String, String> isinCodeToName) {
        Map<Integer, Isin> isinById = new HashMap<>();
        Set<Integer> isinIds = new HashSet<>();
        for (var entry : isinCodeToName.entrySet()) {
            String name = entry.getValue();
            if (name == null || name.isBlank()) {
                // skip - but still need to continue instead of break/continue
                // filter below handles this
            }
            Isin isin = isinMap.get(entry.getKey());
            if (isin != null) {
                isinIds.add(isin.getId());
                isinById.put(isin.getId(), isin);
            }
        }
        if (isinIds.isEmpty()) return;

        Set<String> existingKeys = new HashSet<>();
        repos.isinName().findByIsinIdIn(isinIds)
            .forEach(in -> existingKeys.add(in.getIsin().getId() + ":" + in.getName()));

        List<IsinName> toInsert = new ArrayList<>();
        for (var entry : isinCodeToName.entrySet()) {
            String name = entry.getValue();
            if (name == null || name.isBlank()) continue;
            Isin isin = isinMap.get(entry.getKey());
            if (isin == null) continue;
            String key = isin.getId() + ":" + name.trim();
            if (existingKeys.add(key)) {
                toInsert.add(IsinName.builder().isin(isin).name(name.trim()).build());
            }
        }
        if (!toInsert.isEmpty()) {
            repos.isinName().saveAll(toInsert);
        }
    }

    private Map<String, Currency> bulkUpsertCurrencies(Set<String> currencyCodes) {
        if (currencyCodes.isEmpty()) return Map.of();

        Map<String, Currency> map = new HashMap<>();
        repos.currency().findByNameIn(currencyCodes).forEach(c -> map.put(c.getName(), c));

        Set<String> missing = new HashSet<>(currencyCodes);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            List<Currency> created = repos.currency().saveAll(
                missing.stream().map(code -> Currency.builder().name(code).build()).toList());
            created.forEach(c -> map.put(c.getName(), c));
        }
        return map;
    }

    static double parseGermanDouble(String s) {
        String v = s.trim();
        v = v.replace(".", "").replace(",", ".").replace("~", ".");
        return parseDouble(v);
    }

    private String[] parseCsvLine(String line, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == separator && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    // ---- DeGiro Transactions.csv ----

    @Transactional
    public ImportResult importDegiroTransactions(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedTransaction> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDegiroTransaction(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Depot depot = repos.depot().findByName("DeGiro").orElseThrow();
            repos.transaction().deleteByDepotId(depot.getId());

            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTransaction::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedTransaction::isinCode, ParsedTransaction::name, (a, b) -> a)));

            List<Transaction> transactions = parsed.stream()
                .map(p -> Transaction.builder()
                    .date(p.date()).isin(isinMap.get(p.isinCode())).depot(depot)
                    .count(p.count()).sharePrice(p.price()).build())
                .toList();

            repos.transaction().saveAll(transactions);
            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} DeGiro transactions in {}", transactions.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(transactions.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedTransaction> parseDegiroTransaction(String line) {
        String[] parts = parseCsvLine(line, ',');
        if (parts.length < 17) return empty();

        String dateStr = parts[0];
        String timeStr = parts[1];
        String product = parts[2];
        String isinCode = parts[3];
        String countStr = parts[6];
        String eurValueStr = parts[11];

        if (isinCode.isBlank()) return empty();

        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        double count = parseGermanDouble(countStr);
        if (count == 0) return empty();

        double eurValue = parseGermanDouble(eurValueStr);
        double price = abs(eurValue) / abs(count);

        return Optional.of(new ParsedTransaction(dateTime, isinCode, product, count, price));
    }

    // ---- ZERO Orders CSV ----

    @Transactional
    public ImportResult importZeroOrders(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedTransaction> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseZeroOrder(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Depot depot = repos.depot().findByName("ZERO").orElseThrow();
            repos.transaction().deleteByDepotId(depot.getId());

            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTransaction::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedTransaction::isinCode, ParsedTransaction::name, (a, b) -> a)));

            List<Transaction> transactions = parsed.stream()
                .map(p -> Transaction.builder()
                    .date(p.date()).isin(isinMap.get(p.isinCode())).depot(depot)
                    .count(p.count()).sharePrice(p.price()).build())
                .toList();

            repos.transaction().saveAll(transactions);
            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} ZERO transactions in {}", transactions.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(transactions.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedTransaction> parseZeroOrder(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 20) return empty();

        String status = parts[5];
        if (!"ausgeführt".equals(status)) return empty();

        String name = parts[0];
        String isinCode = parts[1];
        String direction = parts[12];
        String execDate = parts[16];
        String execTime = parts[17];
        String execPrice = parts[18];
        String execCount = parts[19];

        if (isinCode.isBlank()) return empty();

        LocalDate date = LocalDate.parse(execDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalTime time = LocalTime.parse(execTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        double count = parseGermanDouble(execCount);
        double price = parseGermanDouble(execPrice);
        if ("Verkauf".equals(direction)) {
            count = -count;
        }

        return Optional.of(new ParsedTransaction(dateTime, isinCode, name, count, price));
    }

    // ---- DeGiro Account.csv (dividends) ----

    @Transactional
    public ImportResult importDegiroAccount(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedDividendPayment> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDegiroAccountRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Depot depot = repos.depot().findByName("DeGiro").orElseThrow();
            repos.dividendPayment().deleteByDepotId(depot.getId());

            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividendPayment::isinCode).collect(toSet()));
            Map<String, Currency> currencyMap = bulkUpsertCurrencies(
                parsed.stream().map(ParsedDividendPayment::currencyCode).collect(toSet()));

            List<DividendPayment> payments = parsed.stream()
                .map(p -> DividendPayment.builder()
                    .timestamp(p.date()).isin(isinMap.get(p.isinCode())).depot(depot)
                    .currency(currencyMap.get(p.currencyCode())).value(p.amount()).build())
                .toList();

            repos.dividendPayment().saveAll(payments);
            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} DeGiro dividend payments in {}", payments.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(payments.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedDividendPayment> parseDegiroAccountRow(String line) {
        String[] parts = parseCsvLine(line, ',');
        if (parts.length < 9) return empty();

        String description = parts[5];
        if (!"Dividende".equals(description)) return empty();

        String valuta = parts[2];
        String isinCode = parts[4];
        String currencyCode = parts[7];
        String amountStr = parts[8];

        if (isinCode.isBlank()) return empty();

        LocalDate date = LocalDate.parse(valuta, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        double amount = parseGermanDouble(amountStr);

        return Optional.of(new ParsedDividendPayment(date.atStartOfDay(), isinCode, currencyCode, amount));
    }

    // ---- ZERO Kontoumsaetze (dividends) ----

    @Transactional
    public ImportResult importZeroAccount(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedDividendPayment> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseZeroAccountRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Depot depot = repos.depot().findByName("ZERO").orElseThrow();
            repos.dividendPayment().deleteByDepotId(depot.getId());

            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividendPayment::isinCode).collect(toSet()));
            Map<String, Currency> currencyMap = bulkUpsertCurrencies(Set.of("EUR"));

            List<DividendPayment> payments = parsed.stream()
                .map(p -> DividendPayment.builder()
                    .timestamp(p.date()).isin(isinMap.get(p.isinCode())).depot(depot)
                    .currency(currencyMap.get("EUR")).value(p.amount()).build())
                .toList();

            repos.dividendPayment().saveAll(payments);
            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} ZERO dividend payments in {}", payments.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(payments.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedDividendPayment> parseZeroAccountRow(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 6) return empty();

        String status = parts[4];
        String purpose = parts[5];
        if (!"gebucht".equals(status) || !purpose.startsWith("Coupons/Dividende")) return empty();

        String valuta = parts[1];
        String amountStr = parts[2];

        int isinIdx = purpose.indexOf("ISIN ");
        if (isinIdx < 0) return empty();
        String isinCode = purpose.substring(isinIdx + 5, isinIdx + 17);

        LocalDate date = LocalDate.parse(valuta, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        double amount = parseGermanDouble(amountStr);

        return Optional.of(new ParsedDividendPayment(date.atStartOfDay(), isinCode, "EUR", amount));
    }

    // ---- dividende.csv ----

    @Transactional
    public ImportResult importDividends(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedDividend> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDividendRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            repos.dividend().deleteAll();

            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividend::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedDividend::isinCode, ParsedDividend::name, (a, b) -> a)));
            Map<String, Currency> currencyMap = bulkUpsertCurrencies(
                parsed.stream().map(ParsedDividend::currencyCode).collect(toSet()));

            List<Dividend> dividends = parsed.stream()
                .map(p -> Dividend.builder()
                    .isin(isinMap.get(p.isinCode())).currency(currencyMap.get(p.currencyCode()))
                    .dividendPerShare(p.dps()).build())
                .toList();

            repos.dividend().saveAll(dividends);
            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} dividend entries in {}", dividends.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(dividends.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedDividend> parseDividendRow(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 4) return empty();

        String isinCode = parts[0];
        String name = parts[1];
        String currencyCode = parts[2];
        String dpsStr = parts[3];

        if (isinCode.isBlank()) return empty();

        double dps = parseGermanDouble(dpsStr);
        return Optional.of(new ParsedDividend(isinCode, name, currencyCode, dps));
    }

    // ---- branches.csv ----

    @Transactional
    public ImportResult importBranches(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedBranch> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseBranchRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedBranch::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedBranch::isinCode, ParsedBranch::name, (a, b) -> a)));

            Map<String, Branch> branchMap = new HashMap<>();
            Set<String> branchNames = parsed.stream().map(ParsedBranch::branchName).collect(toSet());
            repos.branch().findByNameIn(branchNames).forEach(b -> branchMap.put(b.getName(), b));
            Set<String> missingBranches = new HashSet<>(branchNames);
            missingBranches.removeAll(branchMap.keySet());
            if (!missingBranches.isEmpty()) {
                repos.branch().saveAll(
                    missingBranches.stream().map(n -> Branch.builder().name(n).build()).toList()
                ).forEach(b -> branchMap.put(b.getName(), b));
            }

            for (ParsedBranch p : parsed) {
                Isin isin = isinMap.get(p.isinCode());
                Branch branch = branchMap.get(p.branchName());
                em.createNativeQuery("DELETE FROM isin_branch WHERE isin_id = :isinId")
                    .setParameter("isinId", isin.getId()).executeUpdate();
                em.createNativeQuery("INSERT INTO isin_branch (isin_id, branch_id) VALUES (:isinId, :branchId)")
                    .setParameter("isinId", isin.getId())
                    .setParameter("branchId", branch.getId()).executeUpdate();
            }

            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} branch mappings in {}", parsed.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(parsed.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedBranch> parseBranchRow(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 3) return empty();

        String isinCode = parts[0];
        String name = parts[1];
        String branchName = parts[2];

        if (isinCode.isBlank() || branchName.isBlank()) return empty();
        return Optional.of(new ParsedBranch(isinCode, name, branchName));
    }

    // ---- countries.csv ----

    @Transactional
    public ImportResult importCountries(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedCountry> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseCountryRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedCountry::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedCountry::isinCode, ParsedCountry::name, (a, b) -> a)));

            Map<String, Country> countryMap = new HashMap<>();
            Set<String> countryNames = parsed.stream().map(ParsedCountry::countryName).collect(toSet());
            repos.country().findByNameIn(countryNames).forEach(c -> countryMap.put(c.getName(), c));
            Set<String> missingCountries = new HashSet<>(countryNames);
            missingCountries.removeAll(countryMap.keySet());
            if (!missingCountries.isEmpty()) {
                repos.country().saveAll(
                    missingCountries.stream().map(n -> Country.builder().name(n).build()).toList()
                ).forEach(c -> countryMap.put(c.getName(), c));
            }

            for (ParsedCountry p : parsed) {
                Isin isin = isinMap.get(p.isinCode());
                Country country = countryMap.get(p.countryName());
                em.createNativeQuery("DELETE FROM isin_country WHERE isin_id = :isinId")
                    .setParameter("isinId", isin.getId()).executeUpdate();
                em.createNativeQuery("INSERT INTO isin_country (isin_id, country_id) VALUES (:isinId, :countryId)")
                    .setParameter("isinId", isin.getId())
                    .setParameter("countryId", country.getId()).executeUpdate();
            }

            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} country mappings in {}", parsed.size(), formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(parsed.size()).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedCountry> parseCountryRow(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 3) return empty();

        String isinCode = parts[0];
        String name = parts[1];
        String countryName = parts[2];

        if (isinCode.isBlank() || countryName.isBlank()) return empty();
        return Optional.of(new ParsedCountry(isinCode, name, countryName));
    }

    // ---- ticker_symbol.csv ----

    @Transactional
    public ImportResult importTickerSymbols(MultipartFile file) {
        long start = System.currentTimeMillis();

        // 1. Read
        List<String> lines = readLines(file);
        if (lines == null) return ImportResult.fail("Error reading file");

        // 2. Parse
        List<String> errors = new ArrayList<>();
        List<ParsedTickerSymbol> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseTickerSymbolRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception e) {
                errors.add("Line " + lineNum + ": " + e.getMessage());
            }
        }

        // 3. Save
        importLock.lock();
        try {
            Map<String, Isin> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTickerSymbol::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream()
                    .filter(p -> !p.name().isBlank())
                    .collect(toMap(ParsedTickerSymbol::isinCode, ParsedTickerSymbol::name, (a, b) -> a)));

            int imported = 0;
            for (ParsedTickerSymbol p : parsed) {
                Isin isin = isinMap.get(p.isinCode());

                TickerSymbol tickerSymbol = repos.tickerSymbol().findBySymbol(p.symbol())
                    .orElseGet(() -> repos.tickerSymbol().save(TickerSymbol.builder()
                        .isin(isin).symbol(p.symbol()).build()));

                if (tickerSymbol.getIsin() == null) {
                    tickerSymbol.setIsin(isin);
                    repos.tickerSymbol().save(tickerSymbol);
                }

                Long count = (Long) em.createNativeQuery(
                        "SELECT COUNT(*) FROM isin_ticker WHERE isin_id = :isinId AND ticker_symbol_id = :tsId")
                    .setParameter("isinId", isin.getId())
                    .setParameter("tsId", tickerSymbol.getId())
                    .getSingleResult();
                if (count == 0) {
                    em.createNativeQuery("INSERT INTO isin_ticker (isin_id, ticker_symbol_id) VALUES (:isinId, :tsId)")
                        .setParameter("isinId", isin.getId())
                        .setParameter("tsId", tickerSymbol.getId())
                        .executeUpdate();
                }
                imported++;
            }

            long duration = System.currentTimeMillis() - start;
            log.info("Imported {} ticker symbol mappings in {}", imported, formatDuration(duration));
            return ImportResult.builder().success(errors.isEmpty()).imported(imported).durationMs(duration).errors(errors).build();
        } finally {
            importLock.unlock();
        }
    }

    private static Optional<ParsedTickerSymbol> parseTickerSymbolRow(String line) {
        if (line.isBlank()) return empty();

        String[] parts = line.split(";", -1);
        if (parts.length < 2) return empty();

        String isinCode = parts[0].trim();
        String symbol = parts[1].trim();
        String name = parts.length >= 3 ? parts[2].trim() : "";

        if (isinCode.isBlank() || symbol.isBlank()) return empty();
        return Optional.of(new ParsedTickerSymbol(isinCode, symbol, name));
    }
}
