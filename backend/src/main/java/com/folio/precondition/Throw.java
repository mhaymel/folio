package com.folio.precondition;

import static com.folio.format.LocalString.format;
import static com.folio.precondition.Precondition.nn;
import static com.folio.precondition.Precondition.notEmpty;

public final class Throw {

    public static final Class<IllegalArgumentException> IAE = IllegalArgumentException.class;
    public static <T> T IAE(String format, Object... args) {
        notEmpty(format);
        nn(args);
        return IAE(format(format, args));
    }

    public static <T> T IAE(String message) {
        throw new IllegalArgumentException(nn(message));
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
