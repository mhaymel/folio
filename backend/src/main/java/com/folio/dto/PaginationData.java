package com.folio.dto;

/**
 * Groups pagination metadata for paginated response DTOs.
 */
record PaginationData(int page, int pageSize, long totalItems, int totalPages) {
}

