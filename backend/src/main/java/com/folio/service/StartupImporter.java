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

@Component
@Profile("dev")
final class StartupImporter implements ApplicationRunner {

    private static final Logger LOG = getLogger(StartupImporter.class);

    private final ImportService importService;
    private final Path samplesDir;

    StartupImporter(ImportService importService,
                    @Value("${folio.startup-import.samples-dir:docs/samples}") String samplesDir) {
        this.importService = requireNonNull(importService);
        this.samplesDir = resolveDir(samplesDir);
    }

    private Path resolveDir(String configured) {
        Path primary = Paths.get(configured);
        if (Files.isDirectory(primary)) return primary;
        Path fallback = Paths.get("..").resolve(configured);
        if (Files.isDirectory(fallback)) return fallback;
        return primary;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!Files.isDirectory(samplesDir)) {
            LOG.warn("Startup import skipped — samples directory not found: {}", samplesDir.toAbsolutePath());
            return;
        }
        LOG.info("Running startup import from {}", samplesDir.toAbsolutePath());

        importFile("countries.csv",     file -> importService.importCountries(file));
        importFile("branches.csv",      file -> importService.importBranches(file));
        importFile("ticker-symbol.csv", file -> importService.importTickerSymbols(file));
        importFile("dividende.csv",     file -> importService.importDividends(file));
        importFile("Account.csv",       file -> importService.importDegiroAccount(file));
        importFile("Transactions.csv",  file -> importService.importDegiroTransactions(file));
        importGlob("ZERO-orders*.csv",          file -> importService.importZeroOrders(file));
        importGlob("ZERO-kontoumsaetze-*.csv",  file -> importService.importZeroAccount(file));

        LOG.info("Startup import complete.");
    }

    private void importFile(String filename, Importer importer) {
        Path file = samplesDir.resolve(filename);
        if (!Files.isRegularFile(file)) {
            LOG.warn("Startup import — file not found, skipping: {}", file);
            return;
        }
        runImport(file, importer);
    }

    private void importGlob(String glob, Importer importer) {
        List<Path> matches;
        try (Stream<Path> stream = Files.list(samplesDir)) {
            String regex = glob.replace("*", ".*");
            matches = stream
                    .filter(path -> path.getFileName().toString().matches(regex))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            LOG.error("Startup import — failed to list {}: {}", samplesDir, exception.getMessage());
            return;
        }
        if (matches.isEmpty()) {
            LOG.warn("Startup import — no files matched glob '{}', skipping.", glob);
            return;
        }
        for (Path file : matches) {
            runImport(file, importer);
        }
    }

    private void runImport(Path file, Importer importer) {
        try {
            LOG.info("Loading {}...", file.getFileName());
            ImportResult result = importer.run(new FileMultipartFile(file));
            LOG.info("Loaded {} — {} rows imported.", file.getFileName(), result.getImported());
        } catch (Exception exception) {
            LOG.error("Startup import failed for {}: {}", file.getFileName(), exception.getMessage(), exception);
        }
    }
}
