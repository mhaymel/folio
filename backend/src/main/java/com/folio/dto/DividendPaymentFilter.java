package com.folio.dto;

import java.time.LocalDate;

public record DividendPaymentFilter(
    String isin,
    String name,
    String depot,
    LocalDate fromDate,
    LocalDate toDate
) {
    public static DividendPaymentFilter none() {
        return new DividendPaymentFilter(null, null, null, null, null);
    }
}