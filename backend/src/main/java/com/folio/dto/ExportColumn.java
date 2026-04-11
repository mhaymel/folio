package com.folio.dto;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Descriptor for a single export column.
 */
public record ExportColumn<T>(String header, Function<T, Object> accessor) {
    public ExportColumn {
        requireNonNull(header);
        requireNonNull(accessor);
    }
}

