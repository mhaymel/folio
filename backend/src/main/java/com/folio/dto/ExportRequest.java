package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Parameter object for export operations.
 */
public record ExportRequest<T>(
    List<T> data,
    List<ExportColumn<T>> columns,
    String format,
    String filenameBase
) {
    public ExportRequest {
        requireNonNull(data);
        requireNonNull(columns);
        requireNonNull(format);
        requireNonNull(filenameBase);
        data = List.copyOf(data);
        columns = List.copyOf(columns);
    }
}

