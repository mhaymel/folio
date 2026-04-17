package com.folio.service;

import com.folio.dto.ImportResult;
import com.folio.dto.ImportStats;
import com.folio.model.BranchEntity;
import com.folio.model.CountryEntity;
import com.folio.model.CurrencyEntity;
import com.folio.model.DepotEntity;
import com.folio.model.DividendEntity;
import com.folio.model.DividendPaymentContext;
import com.folio.model.DividendPaymentEntity;
import com.folio.model.DividendPaymentEntityValues;
import com.folio.model.DividendReference;
import com.folio.model.IsinEntity;
import com.folio.model.IsinNameEntity;
import com.folio.model.TickerSymbolEntity;
import com.folio.model.TransactionContext;
import com.folio.model.TransactionEntity;
import com.folio.model.TransactionValues;
import com.folio.parser.ParsedBranch;
import com.folio.parser.ParsedCountry;
import com.folio.parser.ParsedDividend;
import com.folio.parser.ParsedDividendPayment;
import com.folio.parser.ParsedTickerSymbol;
import com.folio.parser.ParsedTransaction;
import com.folio.repository.ImportRepositories;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.requireNonNull;

@Service
public class ImportService {

    private static final Logger LOG = getLogger(ImportService.class);
    private static final DateTimeFormatter DATE_DASH_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_DOT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_HM_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_HMS_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Serialises all import operations so that concurrent HTTP requests do not
     * produce lock-timeout errors on the shared {@code isin} table.
     * Each {@code @Transactional} import method acquires this lock before
     * touching the database and releases it in a {@code finally} block.
     */
    private final ReentrantLock importLock;

    private final ImportRepositories repositories;
    private final EntityManager entityManager;

    private ImportService(ReentrantLock importLock, ImportRepositories repositories, EntityManager entityManager) {
        this.importLock = requireNonNull(importLock);
        this.repositories = requireNonNull(repositories);
        this.entityManager = requireNonNull(entityManager);
    }

    @Autowired
    public ImportService(ImportRepositories repositories, EntityManager entityManager) {
        this(new ReentrantLock(), repositories, entityManager);
    }

    private String formatDuration(long ms) {
        if (ms >= 1000) {
            return (ms / 1000) + "s " + (ms % 1000) + "ms";
        }
        return ms + "ms";
    }

    private List<String> readLines(MultipartFile file) {
        long start = now();
        try (InputStream is = file.getInputStream()) {
            List<String> lines = readLinesFrom(is);
            LOG.info("Read {} lines in {} ms", lines.size(), now() - start);
            return lines;
        } catch (Exception exception) {
            LOG.warn("Failed to read file: {}", exception.getMessage());
        }
        return emptyList();
    }

    private List<String> readLinesFrom(InputStream is) {
        return bufferedReader(is).lines().toList();
    }

    private BufferedReader bufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, UTF_8));
    }

    private Map<String, IsinEntity> bulkUpsertIsins(Set<String> isinCodes) {
        if (isinCodes.isEmpty()) return Map.of();

        Map<String, IsinEntity> map = new HashMap<>();
        repositories.isin().findByIsinIn(isinCodes).forEach(isinEntity -> map.put(isinEntity.getIsin(), isinEntity));

        Set<String> missing = new HashSet<>(isinCodes);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            List<IsinEntity> created = repositories.isin().saveAll(
                missing.stream().map(isinCode -> new IsinEntity(null, isinCode)).toList());
            created.forEach(isinEntity -> map.put(isinEntity.getIsin(), isinEntity));
        }
        return map;
    }

    private void bulkUpsertIsinNames(Map<String, IsinEntity> isinMap, Map<String, String> isinCodeToName) {
        Set<Integer> isinIds = new HashSet<>();
        for (var entry : isinCodeToName.entrySet()) {
            IsinEntity isin = isinMap.get(entry.getKey());
            if (isin != null) {
                isinIds.add(isin.getId());
            }
        }
        if (isinIds.isEmpty()) return;

        Set<String> existingKeys = new HashSet<>();
        repositories.isinName().findByIsinIdIn(isinIds)
            .forEach(isinName -> existingKeys.add(isinName.getIsin().getId() + ":" + isinName.getName()));

        List<IsinNameEntity> toInsert = new ArrayList<>();
        for (var entry : isinCodeToName.entrySet()) {
            buildIsinName(entry, isinMap, existingKeys).ifPresent(toInsert::add);
        }
        if (!toInsert.isEmpty()) {
            repositories.isinName().saveAll(toInsert);
        }
    }

    private Optional<IsinNameEntity> buildIsinName(Map.Entry<String, String> entry,
                                                    Map<String, IsinEntity> isinMap,
                                                    Set<String> existingKeys) {
        String name = entry.getValue();
        if (name == null || name.isBlank()) return Optional.empty();
        IsinEntity isin = isinMap.get(entry.getKey());
        if (isin == null) return Optional.empty();
        String key = isin.getId() + ":" + name.trim();
        if (!existingKeys.add(key)) return Optional.empty();
        return Optional.of(new IsinNameEntity(null, isin, name.trim()));
    }

    private Map<String, CurrencyEntity> bulkUpsertCurrencies(Set<String> currencyCodes) {
        if (currencyCodes.isEmpty()) return Map.of();

        Map<String, CurrencyEntity> map = new HashMap<>();
        repositories.currency().findByNameIn(currencyCodes).forEach(currency -> map.put(currency.getName(), currency));

        Set<String> missing = new HashSet<>(currencyCodes);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            List<CurrencyEntity> created = repositories.currency().saveAll(
                missing.stream().map(currencyCode -> new CurrencyEntity(null, currencyCode)).toList());
            created.forEach(currency -> map.put(currency.getName(), currency));
        }
        return map;
    }

    static double parseGermanDouble(String input) {
        String cleaned = input.trim();
        cleaned = cleaned.replace(".", "").replace(",", ".").replace("~", ".");
        return parseDouble(cleaned);
    }

    private String[] parseCsvLine(String line, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                inQuotes = !inQuotes;
            } else if (character == separator && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(character);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }
    @Transactional
    public ImportResult importDegiroTransactions(MultipartFile file) {
        long start = now();
        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedTransaction> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDegiroTransaction(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            DepotEntity depot = repositories.depot().findByName("DeGiro").orElseThrow();
            repositories.transaction().deleteByDepotId(depot.getId());

            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTransaction::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedTransaction::isinCode, ParsedTransaction::name, (first, second) -> first)));

            List<TransactionEntity> transactions = parsed.stream()
                .map(parsedTransaction -> new TransactionEntity(null,
                    new TransactionContext(parsedTransaction.date(), isinMap.get(parsedTransaction.isinCode()), depot),
                    new TransactionValues(parsedTransaction.count(), parsedTransaction.price())))
                .toList();

            repositories.transaction().saveAll(transactions);
            long duration = now() - start;
            LOG.info("Imported {} DeGiro transactions in {}", transactions.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(transactions.size(), duration), errors);
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

        LocalDate date = LocalDate.parse(dateStr, DATE_DASH_FORMAT);
        LocalTime time = LocalTime.parse(timeStr, TIME_HM_FORMAT);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        double count = parseGermanDouble(countStr);
        if (count == 0) return empty();

        double eurValue = parseGermanDouble(eurValueStr);
        double price = abs(eurValue) / abs(count);

        return Optional.of(new ParsedTransaction(dateTime, isinCode, product, count, price));
    }
    @Transactional
    public ImportResult importZeroOrders(MultipartFile file) {
        long start = now();

        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedTransaction> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseZeroOrder(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            DepotEntity depot = repositories.depot().findByName("ZERO").orElseThrow();
            repositories.transaction().deleteByDepotId(depot.getId());

            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTransaction::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedTransaction::isinCode, ParsedTransaction::name, (first, second) -> first)));

            List<TransactionEntity> transactions = parsed.stream()
                .map(parsedTransaction -> new TransactionEntity(null,
                    new TransactionContext(parsedTransaction.date(), isinMap.get(parsedTransaction.isinCode()), depot),
                    new TransactionValues(parsedTransaction.count(), parsedTransaction.price())))
                .toList();

            repositories.transaction().saveAll(transactions);
            long duration = now() - start;
            LOG.info("Imported {} ZERO transactions in {}", transactions.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(transactions.size(), duration), errors);
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

        LocalDate date = LocalDate.parse(execDate, DATE_DOT_FORMAT);
        LocalTime time = LocalTime.parse(execTime, TIME_HMS_FORMAT);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        double count = parseGermanDouble(execCount);
        double price = parseGermanDouble(execPrice);
        if ("Verkauf".equals(direction)) {
            count = -count;
        }

        return Optional.of(new ParsedTransaction(dateTime, isinCode, name, count, price));
    }

    @Transactional
    public ImportResult importDegiroAccount(MultipartFile file) {
        long start = now();

        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedDividendPayment> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDegiroAccountRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            DepotEntity depot = repositories.depot().findByName("DeGiro").orElseThrow();
            repositories.dividendPayment().deleteByDepotId(depot.getId());

            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividendPayment::isinCode).collect(toSet()));
            Map<String, CurrencyEntity> currencyMap = bulkUpsertCurrencies(
                parsed.stream().map(ParsedDividendPayment::currencyCode).collect(toSet()));

            List<DividendPaymentEntity> payments = parsed.stream()
                .map(parsed1 -> new DividendPaymentEntity(null,
                    new DividendPaymentContext(parsed1.date(), isinMap.get(parsed1.isinCode()), depot),
                    new DividendPaymentEntityValues(currencyMap.get(parsed1.currencyCode()), parsed1.amount())))
                .toList();

            repositories.dividendPayment().saveAll(payments);
            long duration = now() - start;
            LOG.info("Imported {} DeGiro dividend payments in {}", payments.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(payments.size(), duration), errors);
        } finally {
            importLock.unlock();
        }
    }

    private long now() {
        return System.currentTimeMillis();
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

        LocalDate date = LocalDate.parse(valuta, DATE_DASH_FORMAT);
        double amount = parseGermanDouble(amountStr);

        return Optional.of(new ParsedDividendPayment(date.atStartOfDay(), isinCode, currencyCode, amount));
    }
    @Transactional
    public ImportResult importZeroAccount(MultipartFile file) {
        long start = now();

        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedDividendPayment> parsed = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseZeroAccountRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            DepotEntity depot = repositories.depot().findByName("ZERO").orElseThrow();
            repositories.dividendPayment().deleteByDepotId(depot.getId());

            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividendPayment::isinCode).collect(toSet()));
            Map<String, CurrencyEntity> currencyMap = bulkUpsertCurrencies(Set.of("EUR"));

            List<DividendPaymentEntity> payments = parsed.stream()
                .map(parsed1 -> new DividendPaymentEntity(null,
                    new DividendPaymentContext(parsed1.date(), isinMap.get(parsed1.isinCode()), depot),
                    new DividendPaymentEntityValues(currencyMap.get("EUR"), parsed1.amount())))
                .toList();

            repositories.dividendPayment().saveAll(payments);
            long duration = now() - start;
            LOG.info("Imported {} ZERO dividend payments in {}", payments.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(payments.size(), duration), errors);
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

        LocalDate date = LocalDate.parse(valuta, DATE_DOT_FORMAT);
        double amount = parseGermanDouble(amountStr);

        return Optional.of(new ParsedDividendPayment(date.atStartOfDay(), isinCode, "EUR", amount));
    }
    @Transactional
    public ImportResult importDividends(MultipartFile file) {
        long start = now();
        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedDividend> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseDividendRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }

        importLock.lock();
        try {
            repositories.dividend().deleteAll();

            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedDividend::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedDividend::isinCode, ParsedDividend::name, (first, second) -> first)));
            Map<String, CurrencyEntity> currencyMap = bulkUpsertCurrencies(
                parsed.stream().map(ParsedDividend::currencyCode).collect(toSet()));

            List<DividendEntity> dividends = parsed.stream()
                .map(parsed1 -> new DividendEntity(null,
                    new DividendReference(isinMap.get(parsed1.isinCode()), currencyMap.get(parsed1.currencyCode())),
                    parsed1.dps()))
                .toList();

            repositories.dividend().saveAll(dividends);
            long duration = now() - start;
            LOG.info("Imported {} dividend entries in {}", dividends.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(dividends.size(), duration), errors);
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
    @Transactional
    public ImportResult importBranches(MultipartFile file) {
        long start = now();
        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedBranch> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseBranchRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedBranch::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedBranch::isinCode, ParsedBranch::name, (first, second) -> first)));

            Map<String, BranchEntity> branchMap = new HashMap<>();
            Set<String> branchNames = parsed.stream().map(ParsedBranch::branchName).collect(toSet());
            repositories.branch().findByNameIn(branchNames).forEach(branch -> branchMap.put(branch.getName(), branch));
            Set<String> missingBranches = new HashSet<>(branchNames);
            missingBranches.removeAll(branchMap.keySet());
            if (!missingBranches.isEmpty()) {
                repositories.branch().saveAll(
                    missingBranches.stream().map(branchName -> new BranchEntity(null, branchName)).toList()
                ).forEach(branch -> branchMap.put(branch.getName(), branch));
            }

            for (ParsedBranch parsedBranch : parsed) {
                IsinEntity isin = isinMap.get(parsedBranch.isinCode());
                BranchEntity branch = branchMap.get(parsedBranch.branchName());
                entityManager.createNativeQuery("DELETE FROM isin_branch WHERE isin_id = :isinId")
                    .setParameter("isinId", isin.getId()).executeUpdate();
                entityManager.createNativeQuery("INSERT INTO isin_branch (isin_id, branch_id) VALUES (:isinId, :branchId)")
                    .setParameter("isinId", isin.getId())
                    .setParameter("branchId", branch.getId()).executeUpdate();
            }

            long duration = now() - start;
            LOG.info("Imported {} branch mappings in {}", parsed.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(parsed.size(), duration), errors);
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
    @Transactional
    public ImportResult importCountries(MultipartFile file) {
        long start = now();
        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedCountry> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseCountryRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedCountry::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream().collect(toMap(ParsedCountry::isinCode, ParsedCountry::name, (first, second) -> first)));

            Map<String, CountryEntity> countryMap = new HashMap<>();
            Set<String> countryNames = parsed.stream().map(ParsedCountry::countryName).collect(toSet());
            repositories.country().findByNameIn(countryNames).forEach(country -> countryMap.put(country.getName(), country));
            Set<String> missingCountries = new HashSet<>(countryNames);
            missingCountries.removeAll(countryMap.keySet());
            if (!missingCountries.isEmpty()) {
                repositories.country().saveAll(
                    missingCountries.stream().map(countryName -> new CountryEntity(null, countryName)).toList()
                ).forEach(country -> countryMap.put(country.getName(), country));
            }

            for (ParsedCountry parsedCountry : parsed) {
                IsinEntity isin = isinMap.get(parsedCountry.isinCode());
                CountryEntity country = countryMap.get(parsedCountry.countryName());
                entityManager.createNativeQuery("DELETE FROM isin_country WHERE isin_id = :isinId")
                    .setParameter("isinId", isin.getId()).executeUpdate();
                entityManager.createNativeQuery("INSERT INTO isin_country (isin_id, country_id) VALUES (:isinId, :countryId)")
                    .setParameter("isinId", isin.getId())
                    .setParameter("countryId", country.getId()).executeUpdate();
            }

            long duration = now() - start;
            LOG.info("Imported {} country mappings in {}", parsed.size(), formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(parsed.size(), duration), errors);
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
    @Transactional
    public ImportResult importTickerSymbols(MultipartFile file) {
        long start = now();
        List<String> lines = readLines(file);
        List<String> errors = new ArrayList<>();
        List<ParsedTickerSymbol> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            try {
                parseTickerSymbolRow(lines.get(i)).ifPresent(parsed::add);
            } catch (Exception exception) {
                errors.add("Line " + lineNum + ": " + exception.getMessage());
            }
        }
        importLock.lock();
        try {
            Map<String, IsinEntity> isinMap = bulkUpsertIsins(
                parsed.stream().map(ParsedTickerSymbol::isinCode).collect(toSet()));
            bulkUpsertIsinNames(isinMap,
                parsed.stream()
                    .filter(ticker -> !ticker.name().isBlank())
                    .collect(toMap(ParsedTickerSymbol::isinCode, ParsedTickerSymbol::name, (first, second) -> first)));

            int imported = 0;
            for (ParsedTickerSymbol parsedTicker : parsed) {
                IsinEntity isin = isinMap.get(parsedTicker.isinCode());

                TickerSymbolEntity tickerSymbol = repositories.tickerSymbol().findBySymbol(parsedTicker.symbol())
                    .orElseGet(() -> repositories.tickerSymbol().save(new TickerSymbolEntity(null, parsedTicker.symbol())));

                Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM isin_ticker WHERE isin_id = :isinId AND ticker_symbol_id = :tsId")
                    .setParameter("isinId", isin.getId())
                    .setParameter("tsId", tickerSymbol.getId())
                    .getSingleResult();
                if (count == 0) {
                    entityManager.createNativeQuery("INSERT INTO isin_ticker (isin_id, ticker_symbol_id) VALUES (:isinId, :tsId)")
                        .setParameter("isinId", isin.getId())
                        .setParameter("tsId", tickerSymbol.getId())
                        .executeUpdate();
                }
                imported++;
            }

            long duration = now() - start;
            LOG.info("Imported {} ticker symbol mappings in {}", imported, formatDuration(duration));
            return new ImportResult(errors.isEmpty(), new ImportStats(imported, duration), errors);
        } finally {
            importLock.unlock();
        }
    }

    private Optional<ParsedTickerSymbol> parseTickerSymbolRow(String line) {
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
