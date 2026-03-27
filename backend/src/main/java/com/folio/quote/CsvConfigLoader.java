package com.folio.quote;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static java.util.Collections.emptyMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads semicolon-delimited config CSV files from the classpath.
 * Format: ISIN;value (e.g. ISIN;url-path or ISIN;TICKER;Name)
 */
public final class CsvConfigLoader {

    private static final Logger log = getLogger(CsvConfigLoader.class);

    /**
     * Load a two-column config: ISIN → value.
     */
    public static Map<String, String> loadTwoColumn(String resourcePath) {
        Map<String, String> map = new HashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.debug("Config file not found: {}", resourcePath);
                return emptyMap();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";", -1);
                    if (parts.length >= 2 && !parts[0].isBlank()) {
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load config {}: {}", resourcePath, e.getMessage());
        }
        return map;
    }

    /**
     * Load a three-column config: ISIN → second column value.
     * Used for isin.symbol.csv: ISIN;TICKER;Name → returns ISIN→TICKER map.
     */
    public static Map<String, String> loadThreeColumnSecond(String resourcePath) {
        Map<String, String> map = new HashMap<>();
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.debug("Config file not found: {}", resourcePath);
                return emptyMap();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";", -1);
                    if (parts.length >= 2 && !parts[0].isBlank()) {
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load config {}: {}", resourcePath, e.getMessage());
        }
        return map;
    }
}

