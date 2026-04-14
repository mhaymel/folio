package com.folio.precondition;

import org.junit.jupiter.api.Test;

import static com.folio.precondition.Assert.assertThrowsIAE;
import static com.folio.precondition.Assert.assertThrowsNPE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class AssertTest {

    @Test
    void shouldPassWhenNPEIsThrown() {
        assertThrowsNPE(() -> { throw new NullPointerException(); });
    }

    @Test
    void shouldFailWhenNoExceptionIsThrown() {
        assertThatThrownBy(() -> assertThrowsNPE(() -> {}))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldFailWhenWrongExceptionTypeIsThrown() {
        assertThatThrownBy(() -> assertThrowsNPE(() -> { throw new IllegalArgumentException(); }))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldPassWhenIAEIsThrown() {
        assertThrowsIAE(() -> { throw new IllegalArgumentException(); });
    }

    @Test
    void shouldFailForIAEWhenNoExceptionIsThrown() {
        assertThatThrownBy(() -> assertThrowsIAE(() -> {}))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldFailForIAEWhenWrongExceptionTypeIsThrown() {
        assertThatThrownBy(() -> assertThrowsIAE(() -> { throw new NullPointerException(); }))
                .isInstanceOf(AssertionError.class);
    }
}
