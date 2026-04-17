package com.folio.service;

/**
 * {@code sortField} is nullable — {@link SortHelper} treats a null or blank
 * value as "no sorting". {@code sortDir} defaults to ascending when null or
 * not equal to "desc".
 */
public record SortRequest(String sortField, String sortDir) {}
