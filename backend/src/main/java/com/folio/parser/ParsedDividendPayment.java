package com.folio.parser;

import java.time.LocalDateTime;

public record ParsedDividendPayment(LocalDateTime date, String isinCode, String currencyCode, double amount) {}