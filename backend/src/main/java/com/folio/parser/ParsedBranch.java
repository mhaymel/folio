package com.folio.parser;

import static java.util.Objects.requireNonNull;

public record ParsedBranch(String isinCode, String name, String branchName) {
    public ParsedBranch {
        requireNonNull(isinCode);
        requireNonNull(name);
        requireNonNull(branchName);
    }
}
