package com.folio.dto;

import java.util.ArrayList;
import java.util.List;

public final class ImportResult {
    private boolean success;
    private int imported;
    private List<String> errors;

    public ImportResult() {
        this.errors = new ArrayList<>();
    }

    public ImportResult(boolean success, int imported, List<String> errors) {
        this.success = success;
        this.imported = imported;
        this.errors = errors;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public static ImportResult ok(int count) {
        return new ImportResult(true, count, new ArrayList<>());
    }

    public static ImportResult fail(List<String> errors) {
        return new ImportResult(false, 0, errors);
    }

    public static ImportResult fail(String error) { return fail(List.of(error)); }

    public static ImportResultBuilder builder() { return new ImportResultBuilder(); }
}