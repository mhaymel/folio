package com.folio.dto;

import java.util.List;

public record StockFiltersDto(List<String> countries, List<String> branches) {}