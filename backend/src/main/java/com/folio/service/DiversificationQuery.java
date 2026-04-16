package com.folio.service;

import static java.util.Objects.requireNonNull;

record DiversificationQuery(String joinTable, String refTable, String fkColumn) {
    DiversificationQuery {
        requireNonNull(joinTable);
        requireNonNull(refTable);
        requireNonNull(fkColumn);
    }
}

