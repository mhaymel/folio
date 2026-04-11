package com.folio.parser;

import static java.util.Objects.requireNonNull;

public record ParsedCountry(String isinCode, String name, String countryName) {
    public ParsedCountry {
        requireNonNull(isinCode);
        requireNonNull(name);
        requireNonNull(countryName);
    }
}
