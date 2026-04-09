package com.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class WallStreetOnline {

    double qouteFor(String path) {
        try {
            Document doc = Jsoup.connect(path)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                    .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                    .timeout(15_000)
                    .get();

            double value = -1.0;
            String currency = "N/A";
            String date = "N/A";

            // 1. Extract price from the main quote box: div.quoteValue > span
            Element quoteValueDiv = doc.selectFirst("div.quoteValue");
            if (quoteValueDiv != null) {
                Element priceSpan = quoteValueDiv.selectFirst("span");
                if (priceSpan != null) {
                    value = parseGermanDouble(priceSpan.text().trim());
                }

                // 2. CurrencyEntity is in the sibling div.quote_currency
                Element parent = quoteValueDiv.parent();
                if (parent != null) {
                    Element currencyDiv = parent.selectFirst("div.quote_currency");
                    if (currencyDiv != null) {
                        String cur = currencyDiv.text().trim();
                        if (!cur.isEmpty() && !cur.equals("%")) {
                            currency = cur;
                        }
                    }
                }
            }

            // 3. Extract date/time: look for "Letzter Kurs" time in the informer header
            Element timeContainer = doc.selectFirst("div.informer_header div.time");
            if (timeContainer != null) {
                Element timeSpan = timeContainer.selectFirst("span[data-push]");
                if (timeSpan != null) {
                    date = timeSpan.text().trim();
                }
            }

            // Fallback: search body for German date pattern (DD.MM.YYYY)
            if ("N/A".equals(date) || date.isEmpty()) {
                String bodyText = doc.body().text();
                Matcher dm = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}(?:\\s+\\d{2}:\\d{2})?)").matcher(bodyText);
                if (dm.find()) {
                    date = dm.group(1);
                }
            }


            System.out.println("Value:    " + value);
            System.out.println("CurrencyEntity: " + currency);
            System.out.println("Date:     " + date);

            return value;

        } catch (IOException e) {
            System.err.println("Error fetching page: " + e.getMessage());
            return -1.0;
        }
    }

    private double parseGermanDouble(String text) {
        try {
            // Remove everything except digits, comma, dot
            String cleaned = text.replaceAll("[^\\d.,]", "").trim();
            if (cleaned.isEmpty()) return -1.0;
            // German format uses comma as decimal separator
            // If both dot and comma exist, the last one is the decimal separator
            if (cleaned.contains(",") && cleaned.contains(".")) {
                // e.g. 1.234,56 -> 1234.56
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else if (cleaned.contains(",")) {
                cleaned = cleaned.replace(",", ".");
            }
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    private String normalizeCurrency(String raw) {
        if (raw == null) return "N/A";
        return switch (raw.trim()) {
            case "€" -> "EUR";
            case "$" -> "USD";
            default -> raw.trim().toUpperCase();
        };
    }
}
