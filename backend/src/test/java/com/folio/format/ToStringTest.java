package com.folio.format;

import org.junit.jupiter.api.Test;

import static com.folio.precondition.Assert.assertThrowsIAE;
import static org.assertj.core.api.Assertions.assertThat;

final class ToStringTest {

    @Test
    void shouldIncludeClassNameAndFields() {
        // given
        var subject = new Simple("hello", 42);

        // when
        String result = ToString.asString(subject);

        // then
        assertThat(result).startsWith("Simple{");
        assertThat(result).contains("text=hello");
        assertThat(result).contains("number=42");
    }

    @Test
    void shouldSkipStaticFields() {
        // given
        var subject = new WithStatic("value");

        // when
        String result = ToString.asString(subject);

        // then
        assertThat(result).doesNotContain("CONSTANT");
        assertThat(result).contains("field=value");
    }

    @Test
    void shouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> ToString.asString(null));
    }


    private static final class Simple {
        private final String text;
        private final int number;

        Simple(String text, int number) {
            this.text = text;
            this.number = number;
        }
    }

    private static final class WithStatic {
        private static final String CONSTANT = "static";
        private final String field;

        WithStatic(String field) {
            this.field = field;
        }
    }
}