package com.folio.parser;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public record ParsedTransaction(LocalDateTime date, String isinCode, String name, double count, double price) {
    public ParsedTransaction {
        requireNonNull(date);
        requireNonNull(isinCode);
        requireNonNull(name);
    }
}
