package com.folio.parser;

import java.time.LocalDateTime;

public record ParsedTransaction(LocalDateTime date, String isinCode, String name, double count, double price) {}