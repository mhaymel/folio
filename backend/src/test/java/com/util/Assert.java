package com.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class Assert {

    private Assert() {
    }

    public static void assertThrowsNPE(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(NullPointerException.class);
    }

    public static void assertThrowsIAE(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(IllegalArgumentException.class);
    }
}