package com.folio.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.folio.domain.Currency.CHF;
import static com.folio.domain.Currency.EUR;
import static com.folio.domain.Currency.GBP;
import static com.folio.domain.Currency.USD;
import static com.folio.domain.Currency.currency;
import static com.util.Assert.assertThrowsIAE;
import static org.assertj.core.api.Assertions.assertThat;

final class CurrencyTest {

    @Test
    void staticConstantsShouldHaveCorrectName() {
        assertThat(USD.name()).isEqualTo("USD");
        assertThat(EUR.name()).isEqualTo("EUR");
        assertThat(GBP.name()).isEqualTo("GBP");
        assertThat(CHF.name()).isEqualTo("CHF");
    }

    @Test
    void currencyFactoryShouldReturnKnownCurrency() {
        // given / when
        Optional<Currency> result = currency("USD");

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(USD);
    }

    @Test
    void currencyFactoryShouldReturnEmptyForUnknownCode() {
        // given / when
        Optional<Currency> result = currency("XXX");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void currencyFactoryShouldThrowIAEForNull() {
        // given / when / then
        assertThrowsIAE(() -> currency(null));
    }

    @Test
    void allSeededCurrenciesShouldBeResolvable() {
        for (String code : new String[]{
                "AUD", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP",
                "HKD", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN",
                "MYR", "NOK", "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB",
                "TRY", "USD", "ZAR"
        }) {
            assertThat(currency(code))
                    .as("currency(%s)", code)
                    .isPresent();
        }
    }

    @Test
    void equalCurrenciesShouldBeEqual() {
        assertThat(currency("EUR").get()).isEqualTo(EUR);
        assertThat(currency("EUR").get().hashCode()).isEqualTo(EUR.hashCode());
    }

    @Test
    void differentCurrenciesShouldNotBeEqual() {
        assertThat(USD).isNotEqualTo(EUR);
    }
}