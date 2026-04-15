package com.folio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

@Service
final class IsinToTicker {

    private static final Logger LOG = getLogger(IsinToTicker.class);
    private static final String OPENFIGI_URL = "https://api.openfigi.com/v3/mapping";
    private static final String API_KEY = "3e4d5c40-6896-42b4-bafc-9ad41bf120fe";
    private static final int BATCH_SIZE = 100;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IsinToTicker() {
        this(HttpClient.newHttpClient(), new ObjectMapper());
    }

    public IsinToTicker(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = requireNonNull(httpClient);
        this.objectMapper = requireNonNull(objectMapper);
    }

    /**
     * Looks up the ticker symbol for a single ISIN via the OpenFIGI Mapping API.
     *
     * @param isin an International Securities Identification Number, e.g. "US0378331005"
     * @return the ticker symbol (e.g. "AAPL") or empty if not found
     */
    public Optional<String> tickerFor(String isin) {
        requireNonNull(isin);
        Map<String, Optional<String>> result = tickersFor(List.of(isin));
        return result.getOrDefault(isin, Optional.empty());
    }

    /**
     * Looks up ticker symbols for multiple ISINs, splitting into batches of at most
     * {@value #BATCH_SIZE} to stay within the OpenFIGI API limit.
     * The returned map preserves insertion order and contains one entry per input ISIN.
     * ISINs that could not be resolved map to {@link Optional#empty()}.
     *
     * @param isins list of ISINs to resolve
     * @return ordered map of ISIN → optional ticker
     */
    public Map<String, Optional<String>> tickersFor(List<String> isins) {
        requireNonNull(isins);

        Map<String, Optional<String>> results = new LinkedHashMap<>();
        if (isins.isEmpty()) {
            return results;
        }

        for (int offset = 0; offset < isins.size(); offset += BATCH_SIZE) {
            List<String> batch = isins.subList(offset, Math.min(offset + BATCH_SIZE, isins.size()));
            results.putAll(fetchBatch(batch));
        }

        return results;
    }

    private Map<String, Optional<String>> fetchBatch(List<String> isins) {
        Map<String, Optional<String>> results = new LinkedHashMap<>();
        try {
            MappingRequest[] requests = isins.stream()
                    .map(isin -> new MappingRequest("ID_ISIN", requireNonNull(isin)))
                    .toArray(MappingRequest[]::new);

            String requestBody = objectMapper.writeValueAsString(requests);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENFIGI_URL))
                    .header("Content-Type", "application/json")
                    .header("X-OPENFIGI-APIKEY", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            LOG.info("POST {} — batch lookup for {} ISIN(s)", OPENFIGI_URL, isins.size());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.warn("OpenFIGI returned HTTP {} for batch of {} ISINs", response.statusCode(), isins.size());
                isins.forEach(isin -> results.put(isin, Optional.empty()));
                return results;
            }

            JsonNode root = objectMapper.readTree(response.body());
            for (int i = 0; i < isins.size(); i++) {
                results.put(isins.get(i), extractTicker(root, i));
            }
        } catch (Exception exception) {
            LOG.warn("OpenFIGI call failed for batch of {} ISINs", isins.size(), exception);
            isins.forEach(isin -> results.putIfAbsent(isin, Optional.empty()));
        }
        return results;
    }

    private Optional<String> extractTicker(JsonNode root, int index) {
        JsonNode entry = root.get(index);
        if (entry == null || !entry.has("data")) {
            return Optional.empty();
        }

        JsonNode data = entry.get("data");
        if (!data.isArray() || data.isEmpty()) {
            return Optional.empty();
        }

        JsonNode tickerNode = data.get(0).get("ticker");
        if (tickerNode == null || tickerNode.isNull()) {
            return Optional.empty();
        }

        return Optional.of(tickerNode.asText());
    }
}
