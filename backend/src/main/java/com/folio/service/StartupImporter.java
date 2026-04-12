package com.folio.service;

import com.folio.dto.ImportResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Imports sample data from {@code docs/samples/} on startup in dev mode.
 * Runs exactly once per application start.
 */
@Component
@Profile("dev")
final class StartupImporter implements ApplicationRunner {

    private static final Logger log = getLogger(StartupImporter.class);

    private final ImportService importService;
    private final Path samplesDir;

    StartupImporter(ImportService importService,
                    @Value("${folio.startup-import.samples-dir:docs/samples}") String samplesDir) {
        this.importService = requireNonNull(importService);
        this.samplesDir = resolveDir(samplesDir);
    }

    private static Path resolveDir(String configured) {
        Path primary = Paths.get(configured);
        if (Files.isDirectory(primary)) return primary;
        // Fallback: try ../docs/samples when working directory is the backend/ subproject
        Path fallback = Paths.get("..").resolve(configured);
        if (Files.isDirectory(fallback)) return fallback;
        return primary; // return as-is so the warning shows the attempted path
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!Files.isDirectory(samplesDir)) {
            log.warn("Startup import skipped — samples directory not found: {}", samplesDir.toAbsolutePath());
            return;
        }
        log.info("Running startup import from {}", samplesDir.toAbsolutePath());

        importFile("countries.csv",     f -> importService.importCountries(f));
        importFile("branches.csv",      f -> importService.importBranches(f));
        importFile("ticker-symbol.csv", f -> importService.importTickerSymbols(f));
        importFile("dividende.csv",     f -> importService.importDividends(f));
        importFile("Account.csv",       f -> importService.importDegiroAccount(f));
        importFile("Transactions.csv",  f -> importService.importDegiroTransactions(f));
        importGlob("ZERO-orders*.csv",          f -> importService.importZeroOrders(f));
        importGlob("ZERO-kontoumsaetze-*.csv",  f -> importService.importZeroAccount(f));

        log.info("Startup import complete.");
    }

    private void importFile(String filename, Importer importer) {
        Path file = samplesDir.resolve(filename);
        if (!Files.isRegularFile(file)) {
            log.warn("Startup import — file not found, skipping: {}", file);
            return;
        }
        runImport(file, importer);
    }

    private void importGlob(String glob, Importer importer) {
        List<Path> matches;
        try (Stream<Path> stream = Files.list(samplesDir)) {
            String regex = glob.replace("*", ".*");
            matches = stream
                    .filter(p -> p.getFileName().toString().matches(regex))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.error("Startup import — failed to list {}: {}", samplesDir, e.getMessage());
            return;
        }
        if (matches.isEmpty()) {
            log.warn("Startup import — no files matched glob '{}', skipping.", glob);
            return;
        }
        for (Path file : matches) {
            runImport(file, importer);
        }
    }

    private void runImport(Path file, Importer importer) {
        try {
            log.info("Loading {}...", file.getFileName());
            ImportResult result = importer.run(new FileMultipartFile(file));
            log.info("Loaded {} — {} rows imported.", file.getFileName(), result.getImported());
        } catch (Exception e) {
            log.error("Startup import failed for {}: {}", file.getFileName(), e.getMessage(), e);
        }
    }

}
