package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Groups reference-data repositories used by import operations.
 */
@Component
record ReferenceImportRepositories(CurrencyRepository currency, CountryRepository country, BranchRepository branch) {
    ReferenceImportRepositories {
        requireNonNull(currency);
        requireNonNull(country);
        requireNonNull(branch);
    }
}

