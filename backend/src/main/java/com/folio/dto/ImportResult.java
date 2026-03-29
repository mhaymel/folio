package com.folio.dto;

import java.util.ArrayList;
import java.util.List;

public final class ImportResult {
    private boolean success;
    private int imported;
    private long durationMs;
    private List<String> errors;

    public ImportResult() {
        this.errors = new ArrayList<>();
    }

    public ImportResult(boolean success, int imported, long durationMs, List<String> errors) {
        this.success = success;
        this.imported = imported;
        this.durationMs = durationMs;
        this.errors = errors;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public static ImportResult ok(int count) {
        return new ImportResult(true, count, 0, new ArrayList<>());
    }

    public static ImportResult fail(List<String> errors) {
        return new ImportResult(false, 0, 0, errors);
    }

    public static ImportResult fail(String error) { return fail(List.of(error)); }

    public static ImportResultBuilder builder() { return new ImportResultBuilder(); }
}