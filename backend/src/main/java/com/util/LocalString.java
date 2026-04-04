package com.util;

import java.util.Locale;

public final class LocalString {

    public static final String NL = format("%n");

    public static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}
