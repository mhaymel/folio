package com.folio.parser;

public record ParsedDividend(String isinCode, String name, String currencyCode, double dps) {}