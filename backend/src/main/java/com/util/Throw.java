package com.util;

import static com.util.LocalString.format;
import static com.util.Precondition.nn;
import static com.util.Precondition.notEmpty;

public final class Throw {

    public static Class<IllegalArgumentException> IAE = IllegalArgumentException.class;
    public static <T> T IAE(String format, Object... args) {
        notEmpty(format);
        nn(args);
        return IAE(format(format, args));
    }

    public static <T> T IAE(String message) {
        nn(message);
        throw new IllegalArgumentException(message);
    }

    public static <T> T ISE(String format, Object... args) {
        notEmpty(format);
        nn(args);
        return ISE(format(format, args));
    }

    public static <T> T ISE(String message) {
        throw new IllegalStateException(message);
    }

    public static <T> T notImplementedYet() {
        throw new IllegalStateException("not implemented yet!");
    }

}
