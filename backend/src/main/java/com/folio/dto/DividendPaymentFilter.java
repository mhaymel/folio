package com.folio.dto;

import java.time.LocalDate;

public record DividendPaymentFilter(
    String isinFragment,
    String nameFragment,
    String depotFragment,
    LocalDate fromDate,
    LocalDate toDate
) {
    public static DividendPaymentFilter none() {
        return new DividendPaymentFilter(null, null, null, null, null);
    }
}