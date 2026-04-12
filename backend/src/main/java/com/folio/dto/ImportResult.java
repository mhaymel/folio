package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class ImportResult {
    private boolean success;
    private final ImportStats stats;
    private List<String> errors;

    public ImportResult() {
        this(false, new ImportStats(0, 0), new ArrayList<>());
    }

    public ImportResult(boolean success, ImportStats stats, List<String> errors) {
        this.success = success;
        this.stats = requireNonNull(stats);
        this.errors = errors;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    @JsonUnwrapped
    public ImportStats getStats() { return stats; }
    public int getImported() { return stats.imported(); }
    public long getDurationMs() { return stats.durationMs(); }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public static ImportResult ok(int count) {
        return new ImportResult(true, new ImportStats(count, 0), new ArrayList<>());
    }

    public static ImportResult fail(List<String> errors) {
        return new ImportResult(false, new ImportStats(0, 0), errors);
    }

    public static ImportResult fail(String error) { return fail(List.of(error)); }

}