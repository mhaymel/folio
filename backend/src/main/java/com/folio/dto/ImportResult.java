package com.folio.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private int imported;
        private List<String> errors = new ArrayList<>();
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder imported(int imported) { this.imported = imported; return this; }
        public Builder errors(List<String> errors) { this.errors = errors; return this; }
        public ImportResult build() { return new ImportResult(success, imported, errors); }
    }
}