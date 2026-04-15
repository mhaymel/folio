package com.folio.controller;

import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
record ListOperations(ExportService exportService, SortHelper sortHelper, PaginationHelper paginationHelper) {
    ListOperations {
        requireNonNull(exportService);
        requireNonNull(sortHelper);
        requireNonNull(paginationHelper);
    }
}

