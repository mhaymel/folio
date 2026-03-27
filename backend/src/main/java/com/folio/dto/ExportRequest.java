package com.folio.dto;

import java.util.List;

/**
 * Parameter object for export operations.
 */
public record ExportRequest<T>(
    List<T> data,
    List<ExportColumn<T>> columns,
    String format,
    String filenameBase
) {}

