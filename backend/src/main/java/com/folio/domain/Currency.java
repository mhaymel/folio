package com.folio.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.util.Precondition.notEmpty;
import static com.util.ToString.asString;

public final class Currency {

    private final String name;

    public static final Currency AUD = new Currency("AUD");
    public static final Currency BRL = new Currency("BRL");
    public static final Currency CAD = new Currency("CAD");
    public static final Currency CHF = new Currency("CHF");
    public static final Currency CNY = new Currency("CNY");
    public static final Currency CZK = new Currency("CZK");
    public static final Currency DKK = new Currency("DKK");
    public static final Currency EUR = new Currency("EUR");
    public static final Currency GBP = new Currency("GBP");
    public static final Currency HKD = new Currency("HKD");
    public static final Currency HUF = new Currency("HUF");
    public static final Currency IDR = new Currency("IDR");
    public static final Currency ILS = new Currency("ILS");
    public static final Currency INR = new Currency("INR");
    public static final Currency ISK = new Currency("ISK");
    public static final Currency JPY = new Currency("JPY");
    public static final Currency KRW = new Currency("KRW");
    public static final Currency MXN = new Currency("MXN");
    public static final Currency MYR = new Currency("MYR");
    public static final Currency NOK = new Currency("NOK");
    public static final Currency NZD = new Currency("NZD");
    public static final Currency PHP = new Currency("PHP");
    public static final Currency PLN = new Currency("PLN");
    public static final Currency RON = new Currency("RON");
    public static final Currency SEK = new Currency("SEK");
    public static final Currency SGD = new Currency("SGD");
    public static final Currency THB = new Currency("THB");
    public static final Currency TRY = new Currency("TRY");
    public static final Currency USD = new Currency("USD");
    public static final Currency ZAR = new Currency("ZAR");

    private static final Map<String, Currency> currencies = Map.ofEntries(
            Map.entry("AUD", AUD), Map.entry("BRL", BRL), Map.entry("CAD", CAD),
            Map.entry("CHF", CHF), Map.entry("CNY", CNY), Map.entry("CZK", CZK),
            Map.entry("DKK", DKK), Map.entry("EUR", EUR), Map.entry("GBP", GBP),
            Map.entry("HKD", HKD), Map.entry("HUF", HUF), Map.entry("IDR", IDR),
            Map.entry("ILS", ILS), Map.entry("INR", INR), Map.entry("ISK", ISK),
            Map.entry("JPY", JPY), Map.entry("KRW", KRW), Map.entry("MXN", MXN),
            Map.entry("MYR", MYR), Map.entry("NOK", NOK), Map.entry("NZD", NZD),
            Map.entry("PHP", PHP), Map.entry("PLN", PLN), Map.entry("RON", RON),
            Map.entry("SEK", SEK), Map.entry("SGD", SGD), Map.entry("THB", THB),
            Map.entry("TRY", TRY), Map.entry("USD", USD), Map.entry("ZAR", ZAR)
    );

    public static Optional<Currency> currency(String name) {
        notEmpty(name);
        return Optional.ofNullable(currencies.get(name.toUpperCase()));
    }

    public String name() {
        return name;
    }

    private Currency(String name) {
        this.name = notEmpty(name).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(name, currency.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return asString(this);
    }
}
