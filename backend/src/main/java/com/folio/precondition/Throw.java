package com.folio.precondition;

import static com.folio.format.LocalString.format;
import static com.folio.precondition.Precondition.notNull;
import static com.folio.precondition.Precondition.notEmpty;

public final class Throw {

    public static <T> T illegalArgument(String format, Object... args) {
        notEmpty(format);
        Precondition.notNull(args);
        return illegalArgument(format(format, args));
    }

    public static <T> T illegalArgument(String message) {
        throw new IllegalArgumentException(Precondition.notNull(message));
    }

    public static <T> T illegalState(String format, Object... args) {
        notEmpty(format);
        Precondition.notNull(args);
        return illegalState(format(format, args));
    }

    public static <T> T illegalState(String message) {
        throw new IllegalStateException(message);
    }

    public static <T> T notImplementedYet() {
        throw new IllegalStateException("not implemented yet!");
    }

}
