package com.folio.controller;

import com.folio.dto.QuoteSettingsDto;
import com.folio.model.Setting;
import com.folio.repository.SettingRepository;
import com.folio.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/quotes")
@Tag(name = "Quotes", description = "Quote fetch management")
public class QuoteController {

    private final SettingRepository settingRepo;
    private final QuoteService quoteService;

    public QuoteController(SettingRepository settingRepo, QuoteService quoteService) {
        this.settingRepo = settingRepo;
        this.quoteService = quoteService;
    }

    @GetMapping("/settings")
    @Operation(summary = "Get quote fetch settings")
    public ResponseEntity<QuoteSettingsDto> getSettings() {
        Integer interval = settingRepo.findByKey("quote.fetch.interval.minutes")
            .map(s -> Integer.parseInt(s.getValue()))
            .orElse(60);

        LocalDateTime lastFetch = settingRepo.findByKey("quote.last.fetch.timestamp")
            .map(s -> LocalDateTime.parse(s.getValue()))
            .orElse(null);

        return ResponseEntity.ok(QuoteSettingsDto.builder()
            .intervalMinutes(interval)
            .lastFetchAt(lastFetch)
            .build());
    }

    @PutMapping("/settings/interval")
    @Operation(summary = "Update quote fetch interval")
    public ResponseEntity<QuoteSettingsDto> updateInterval(@RequestBody Map<String, Integer> body) {
        Integer minutes = body.get("intervalMinutes");
        if (minutes == null || minutes < 1) {
            throw new IllegalArgumentException("intervalMinutes must be a positive integer");
        }

        Setting setting = settingRepo.findByKey("quote.fetch.interval.minutes")
            .orElseGet(() -> Setting.builder().key("quote.fetch.interval.minutes").value("60").build());
        setting.setValue(String.valueOf(minutes));
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