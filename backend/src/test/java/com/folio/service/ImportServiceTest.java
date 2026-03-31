package com.folio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

final class ImportServiceTest {

    @ParameterizedTest
    @CsvSource({
        // simple integers
        "1,        1.0",
        "42,       42.0",
        // German decimal comma
        "'3,40',   3.4",
        "'29,53',  29.53",
        // thousands separator (dot) without decimal
        "'1.000',  1000.0",
        "'12.345', 12345.0",
        // thousands separator with decimal comma
        "'1.000,50',   1000.5",
        "'12.345,67',  12345.67",
        "'1.234.567',  1234567.0",
        "'1.234.567,89', 1234567.89",
        // tilde as decimal separator (DeGiro Account.csv)
        "'5~25',   5.25",
        // whitespace trimming
        "' 3,40 ', 3.4",
    })
    void parsesGermanFormats(String input, double expected) {
        assertThat(ImportService.parseGermanDouble(input)).isEqualTo(expected);
    }

    @Test
    void parsesZeroValue() {
        assertThat(ImportService.parseGermanDouble("0")).isEqualTo(0.0);
        assertThat(ImportService.parseGermanDouble("0,00")).isEqualTo(0.0);
    }
}