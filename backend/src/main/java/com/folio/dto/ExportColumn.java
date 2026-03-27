package com.folio.dto;

import java.util.function.Function;

/**
 * Descriptor for a single export column.
 */
public record ExportColumn<T>(String header, Function<T, Object> accessor) {}

