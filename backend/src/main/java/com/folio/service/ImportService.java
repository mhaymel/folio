package com.folio.service;

import com.folio.dto.ImportResult;
import com.folio.model.*;
import com.folio.repository.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final IsinRepository isinRepo;
    private final IsinNameRepository isinNameRepo;
    private final DepotRepository depotRepo;
    private final CurrencyRepository currencyRepo;
    private final TransactionRepository transactionRepo;
    private final DividendRepository dividendRepo;
    private final DividendPaymentRepository dividendPaymentRepo;
    private final CountryRepository countryRepo;
    private final BranchRepository branchRepo;
    private final TickerSymbolRepository tickerSymbolRepo;
    private final EntityManager em;

    public ImportService(IsinRepository isinRepo, IsinNameRepository isinNameRepo, DepotRepository depotRepo,
                         CurrencyRepository currencyRepo, TransactionRepository transactionRepo,
                         DividendRepository dividendRepo, DividendPaymentRepository dividendPaymentRepo,
                         CountryRepository countryRepo, BranchRepository branchRepo,
                         TickerSymbolRepository tickerSymbolRepo, EntityManager em) {
        this.isinRepo = isinRepo;
        this.isinNameRepo = isinNameRepo;
        this.depotRepo = depotRepo;
        this.currencyRepo = currencyRepo;
        this.transactionRepo = transactionRepo;
        this.dividendRepo = dividendRepo;
        this.dividendPaymentRepo = dividendPaymentRepo;
        this.countryRepo = countryRepo;
        this.branchRepo = branchRepo;
        this.tickerSymbolRepo = tickerSymbolRepo;
        this.em = em;
    }

    private Isin upsertIsin(String isinCode) {
        return isinRepo.findByIsin(isinCode)
            .orElseGet(() -> isinRepo.save(Isin.builder().isin(isinCode).build()));
    }

    private void upsertIsinName(Isin isin, String name) {
        if (name != null && !name.isBlank() && !isinNameRepo.existsByIsinIdAndName(isin.getId(), name)) {
            isinNameRepo.save(IsinName.builder().isin(isin).name(name.trim()).build());
        }
    }

    private Currency upsertCurrency(String code) {
        return currencyRepo.findByName(code)
            .orElseGet(() -> currencyRepo.save(Currency.builder().name(code).build()));
    }

    private double parseDouble(String s) {
        String v = s.trim();
        // German locale: '.' = thousands separator, ',' or '~' = decimal separator
        // e.g. "1.234,56" or "1.234~56" -> "1234.56"
        if (v.contains(",") || v.contains("~")) {
            v = v.replace(".", "").replace(",", ".").replace("~", ".");
        }
        return Double.parseDouble(v);
    }

    // ---- DeGiro Transactions.csv ----
    @Transactional
    public ImportResult importDegiroTransactions(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        Depot depot = depotRepo.findByName("DeGiro").orElseThrow();
        transactionRepo.deleteByDepotId(depot.getId());

        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    // CSV is comma-separated but values can be quoted
                    String[] parts = parseCsvLine(line, ',');
                    if (parts.length < 17) continue;

                    String dateStr = parts[0]; // DD-MM-YYYY
                    String timeStr = parts[1]; // HH:mm
                    String product = parts[2];
                    String isinCode = parts[3];
                    String countStr = parts[6];
                    String eurValueStr = parts[11]; // Wert EUR — total trade value in EUR (negative for buys)

                    if (isinCode.isBlank()) continue;

                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    LocalDateTime dateTime = LocalDateTime.of(date, time);

                    double count = parseDouble(countStr);
                    if (count == 0) continue; // skip non-trade rows (fees, dividends, etc.)

                    // Derive EUR share price from total EUR value to guarantee EUR denomination
                    // regardless of the security's trading currency (Kurs at index 7 may be USD etc.)
                    double eurValue = parseDouble(eurValueStr);
                    double price = Math.abs(eurValue) / Math.abs(count);

                    Isin isin = upsertIsin(isinCode);
                    upsertIsinName(isin, product);

                    transactions.add(Transaction.builder()
                        .date(dateTime).isin(isin).depot(depot)
                        .count(count).sharePrice(price).build());
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        transactionRepo.saveAll(transactions);
        log.info("Imported {} DeGiro transactions", transactions.size());
        return ImportResult.builder().success(errors.isEmpty()).imported(transactions.size()).errors(errors).build();
    }

    // ---- ZERO Orders CSV ----
    @Transactional
    public ImportResult importZeroOrders(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        Depot depot = depotRepo.findByName("ZERO").orElseThrow();
        transactionRepo.deleteByDepotId(depot.getId());

        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = line.split(";", -1);
                    if (parts.length < 20) continue;

                    String status = parts[5];
                    if (!"ausgeführt".equals(status)) continue;

                    String name = parts[0];
                    String isinCode = parts[1];
                    String direction = parts[12]; // Kauf / Verkauf
                    String execDate = parts[16]; // DD.MM.YYYY
                    String execTime = parts[17]; // HH:mm:ss
                    String execPrice = parts[18];
                    String execCount = parts[19];

                    if (isinCode.isBlank()) continue;

                    LocalDate date = LocalDate.parse(execDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    LocalTime time = LocalTime.parse(execTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
                    LocalDateTime dateTime = LocalDateTime.of(date, time);

                    double count = parseDouble(execCount);
                    double price = parseDouble(execPrice);
                    if ("Verkauf".equals(direction)) {
                        count = -count;
                    }

                    Isin isin = upsertIsin(isinCode);
                    upsertIsinName(isin, name);

                    transactions.add(Transaction.builder()
                        .date(dateTime).isin(isin).depot(depot)
                        .count(count).sharePrice(price).build());
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        transactionRepo.saveAll(transactions);
        log.info("Imported {} ZERO transactions", transactions.size());
        return ImportResult.builder().success(errors.isEmpty()).imported(transactions.size()).errors(errors).build();
    }

    // ---- DeGiro Account.csv (dividends) ----
    @Transactional
    public ImportResult importDegiroAccount(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        Depot depot = depotRepo.findByName("DeGiro").orElseThrow();
        dividendPaymentRepo.deleteByDepotId(depot.getId());

        List<DividendPayment> payments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = parseCsvLine(line, ',');
                    if (parts.length < 9) continue;

                    String description = parts[5];
                    if (!"Dividende".equals(description)) continue;

                    String valuta = parts[2]; // DD-MM-YYYY
                    String isinCode = parts[4];
                    String currencyCode = parts[7];
                    String amountStr = parts[8];

                    if (isinCode.isBlank()) continue;

                    LocalDate date = LocalDate.parse(valuta, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    double amount = parseDouble(amountStr);

                    Isin isin = upsertIsin(isinCode);
                    Currency currency = upsertCurrency(currencyCode);

                    payments.add(DividendPayment.builder()
                        .timestamp(date.atStartOfDay()).isin(isin).depot(depot)
                        .currency(currency).value(amount).build());
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        dividendPaymentRepo.saveAll(payments);
        log.info("Imported {} DeGiro dividend payments", payments.size());
        return ImportResult.builder().success(errors.isEmpty()).imported(payments.size()).errors(errors).build();
    }

    // ---- ZERO Kontoumsaetze (dividends) ----
    @Transactional
    public ImportResult importZeroAccount(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        Depot depot = depotRepo.findByName("ZERO").orElseThrow();
        dividendPaymentRepo.deleteByDepotId(depot.getId());
        Currency eur = upsertCurrency("EUR");

        List<DividendPayment> payments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = line.split(";", -1);
                    if (parts.length < 6) continue;

                    String status = parts[4];
                    String purpose = parts[5];
                    if (!"gebucht".equals(status) || !purpose.startsWith("Coupons/Dividende")) continue;

                    String valuta = parts[1]; // DD.MM.YYYY
                    String amountStr = parts[2];

                    // Extract ISIN from purpose: find "ISIN " then take 12 chars
                    int isinIdx = purpose.indexOf("ISIN ");
                    if (isinIdx < 0) continue;
                    String isinCode = purpose.substring(isinIdx + 5, isinIdx + 17);

                    LocalDate date = LocalDate.parse(valuta, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    double amount = parseDouble(amountStr);

                    Isin isin = upsertIsin(isinCode);

                    payments.add(DividendPayment.builder()
                        .timestamp(date.atStartOfDay()).isin(isin).depot(depot)
                        .currency(eur).value(amount).build());
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        dividendPaymentRepo.saveAll(payments);
        log.info("Imported {} ZERO dividend payments", payments.size());
        return ImportResult.builder().success(errors.isEmpty()).imported(payments.size()).errors(errors).build();
    }

    // ---- dividende.csv ----
    @Transactional
    public ImportResult importDividends(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        dividendRepo.deleteAll();

        List<Dividend> dividends = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = line.split(";", -1);
                    if (parts.length < 4) continue;

                    String isinCode = parts[0];
                    String name = parts[1];
                    String currencyCode = parts[2];
                    String dpsStr = parts[3];

                    if (isinCode.isBlank()) continue;

                    Isin isin = upsertIsin(isinCode);
                    upsertIsinName(isin, name);
                    Currency currency = upsertCurrency(currencyCode);
                    double dps = parseDouble(dpsStr);

                    dividends.add(Dividend.builder()
                        .isin(isin).currency(currency).dividendPerShare(dps).build());
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        dividendRepo.saveAll(dividends);
        log.info("Imported {} dividend entries", dividends.size());
        return ImportResult.builder().success(errors.isEmpty()).imported(dividends.size()).errors(errors).build();
    }

    // ---- branches.csv ----
    @Transactional
    public ImportResult importBranches(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = line.split(";", -1);
                    if (parts.length < 3) continue;

                    String isinCode = parts[0];
                    String name = parts[1];
                    String branchName = parts[2];

                    if (isinCode.isBlank() || branchName.isBlank()) continue;

                    Isin isin = upsertIsin(isinCode);
                    upsertIsinName(isin, name);

                    Branch branch = branchRepo.findByName(branchName)
                        .orElseGet(() -> branchRepo.save(Branch.builder().name(branchName).build()));

                    // Upsert isin_branch: delete old, insert new (1:1 assumption)
                    em.createNativeQuery("DELETE FROM isin_branch WHERE isin_id = :isinId")
                        .setParameter("isinId", isin.getId()).executeUpdate();
                    em.createNativeQuery("INSERT INTO isin_branch (isin_id, branch_id) VALUES (:isinId, :branchId)")
                        .setParameter("isinId", isin.getId())
                        .setParameter("branchId", branch.getId()).executeUpdate();

                    imported++;
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        log.info("Imported {} branch mappings", imported);
        return ImportResult.builder().success(errors.isEmpty()).imported(imported).errors(errors).build();
    }

    // ---- countries.csv ----
    @Transactional
    public ImportResult importCountries(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = line.split(";", -1);
                    if (parts.length < 3) continue;

                    String isinCode = parts[0];
                    String name = parts[1];
                    String countryName = parts[2];

                    if (isinCode.isBlank() || countryName.isBlank()) continue;

                    Isin isin = upsertIsin(isinCode);
                    upsertIsinName(isin, name);

                    Country country = countryRepo.findByName(countryName)
                        .orElseGet(() -> countryRepo.save(Country.builder().name(countryName).build()));

                    // Upsert isin_country: delete old, insert new (1:1 assumption)
                    em.createNativeQuery("DELETE FROM isin_country WHERE isin_id = :isinId")
                        .setParameter("isinId", isin.getId()).executeUpdate();
                    em.createNativeQuery("INSERT INTO isin_country (isin_id, country_id) VALUES (:isinId, :countryId)")
                        .setParameter("isinId", isin.getId())
                        .setParameter("countryId", country.getId()).executeUpdate();

                    imported++;
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        log.info("Imported {} country mappings", imported);
        return ImportResult.builder().success(errors.isEmpty()).imported(imported).errors(errors).build();
    }

    // ---- ticker_symbol.csv ----
    // Format: ISIN;TickerSymbol;Name (Name is optional)
    @Transactional
    public ImportResult importTickerSymbols(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    if (line.isBlank()) continue;

                    String[] parts = line.split(";", -1);
                    if (parts.length < 2) continue;

                    String isinCode = parts[0].trim();
                    String symbol = parts[1].trim();
                    String name = parts.length >= 3 ? parts[2].trim() : "";

                    if (isinCode.isBlank() || symbol.isBlank()) continue;

                    // Upsert ISIN — creates it if not yet in the isin table
                    Isin isin = upsertIsin(isinCode);

                    // If name is present, add it to isin_name
                    if (!name.isBlank()) {
                        upsertIsinName(isin, name);
                    }

                    // Find or create ticker symbol
                    TickerSymbol tickerSymbol = tickerSymbolRepo.findBySymbol(symbol)
                        .orElseGet(() -> tickerSymbolRepo.save(TickerSymbol.builder()
                            .isin(isin).symbol(symbol).build()));

                    // Update isin reference if not set
                    if (tickerSymbol.getIsin() == null) {
                        tickerSymbol.setIsin(isin);
                        tickerSymbolRepo.save(tickerSymbol);
                    }

                    // Upsert isin_ticker mapping
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
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ImportResult.fail(List.of("Failed to read file: " + e.getMessage()));
        }

        log.info("Imported {} ticker symbol mappings", imported);
        return ImportResult.builder().success(errors.isEmpty()).imported(imported).errors(errors).build();
    }

    // Simple CSV line parser that handles quoted fields
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
}