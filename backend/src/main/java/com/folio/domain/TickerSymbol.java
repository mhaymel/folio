package com.folio.domain;

import static com.util.Precondition.noWhitespaces;
import static com.util.Precondition.notEmpty;

public record TickerSymbol(String value) {
      public TickerSymbol(String value) {
          this.value = notEmpty(noWhitespaces(value)).toUpperCase();
      }
}
