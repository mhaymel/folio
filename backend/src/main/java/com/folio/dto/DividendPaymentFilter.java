package com.folio.dto;

import com.folio.precondition.Precondition;

import java.time.LocalDate;

import static com.folio.precondition.Precondition.notNull;

public record DividendPaymentFilter(
    String isinFragment,
    String nameFragment,
    String depotFragment,
    LocalDate fromDate,
    LocalDate toDate
) {
    public DividendPaymentFilter {
        notNull(isinFragment);
        notNull(nameFragment);
        notNull(depotFragment);
    }
    public static DividendPaymentFilter none() {
        return new DividendPaymentFilter("", "", "", null, null);
    }
}