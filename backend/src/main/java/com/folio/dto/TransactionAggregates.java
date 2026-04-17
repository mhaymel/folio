package com.folio.dto;

import static com.folio.precondition.Precondition.notNull;

record TransactionAggregates(long filteredCount, Double sumCount) {
    TransactionAggregates {
        notNull(sumCount);
    }
}
