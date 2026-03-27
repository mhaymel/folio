package com.folio.dto;

import java.util.ArrayList;
import java.util.List;

public final class ImportResultBuilder {
    private boolean success;
    private int imported;
    private List<String> errors = new ArrayList<>();

    public ImportResultBuilder success(boolean success) { this.success = success; return this; }
    public ImportResultBuilder imported(int imported) { this.imported = imported; return this; }
    public ImportResultBuilder errors(List<String> errors) { this.errors = errors; return this; }
    public ImportResult build() { return new ImportResult(success, imported, errors); }
}

