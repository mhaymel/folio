package com.folio.dto;

import java.time.LocalDate;

/**
 * Filter criteria for querying dividend payments. Every component is nullable —
 * a null value means "no constraint on that field" (see {@link #none()}).
 */
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