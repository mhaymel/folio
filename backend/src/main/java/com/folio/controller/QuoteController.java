package com.folio.controller;

import static com.util.Throw.IAE;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

import com.folio.dto.QuoteSettingsDto;
import com.folio.model.SettingEntity;
import com.folio.repository.SettingRepository;
import com.folio.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/quotes")
@Tag(name = "Quotes", description = "Quote fetch management")
public final class QuoteController {

    private final SettingRepository settingRepo;
    private final QuoteService quoteService;

    public QuoteController(SettingRepository settingRepo, QuoteService quoteService) {
        this.settingRepo = requireNonNull(settingRepo);
        this.quoteService = requireNonNull(quoteService);
    }

    @GetMapping("/settings")
    @Operation(summary = "Get quote fetch settings")
    public ResponseEntity<QuoteSettingsDto> getSettings() {
        boolean enabled = settingRepo.findByKey("quote.fetch.enabled")
            .map(s -> Boolean.parseBoolean(s.getValue()))
            .orElse(false);

        Integer interval = settingRepo.findByKey("quote.fetch.interval.minutes")
            .map(s -> parseInt(s.getValue()))
            .orElse(60);

        LocalDateTime lastFetch = settingRepo.findByKey("quote.last.fetch.timestamp")
            .map(s -> LocalDateTime.parse(s.getValue()))
            .orElse(null);

        String lastFetchFormatted = lastFetch != null
            ? lastFetch.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            : null;

        return ResponseEntity.ok(new QuoteSettingsDto(enabled, interval, lastFetchFormatted));
    }

    @PutMapping("/settings/enabled")
    @Operation(summary = "Enable or disable automatic quote fetching")
    public ResponseEntity<QuoteSettingsDto> updateEnabled(@RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            IAE("enabled must be provided");
        }

        SettingEntity setting = settingRepo.findByKey("quote.fetch.enabled")
            .orElseGet(() -> new SettingEntity(null, "quote.fetch.enabled", "false"));
        setting.setValue(valueOf(enabled));
        settingRepo.save(setting);

        return getSettings();
    }

    @PutMapping("/settings/interval")
    @Operation(summary = "Update quote fetch interval")
    public ResponseEntity<QuoteSettingsDto> updateInterval(@RequestBody Map<String, Integer> body) {
        Integer minutes = body.get("intervalMinutes");
        if (minutes == null || minutes < 1) {
            IAE("intervalMinutes must be a positive integer");
        }

        SettingEntity setting = settingRepo.findByKey("quote.fetch.interval.minutes")
            .orElseGet(() -> new SettingEntity(null, "quote.fetch.interval.minutes", "60"));
        setting.setValue(valueOf(minutes));
        settingRepo.save(setting);

        return getSettings();
    }

    @PostMapping("/fetch")
    @Operation(summary = "Trigger immediate quote fetch for all held ISINs")
    public ResponseEntity<Map<String, Object>> triggerFetch() {
        int fetched = quoteService.triggerFetch();
        return ResponseEntity.ok(Map.of(
            "status", "completed",
            "fetchedCount", fetched
        ));
    }
}