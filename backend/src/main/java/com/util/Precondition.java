package com.util;

import java.math.BigInteger;
import java.util.*;

import static com.util.Throw.IAE;
import static java.lang.String.format;

public final class Precondition {

    public static final Class<IllegalArgumentException> IAE = IllegalArgumentException.class;

    public static byte[] notEmpty(byte[] bytes) {
        nn(bytes);
        if (bytes.length > 0) {
            return bytes;
        }
        return IAE("byte array must not be empty");
    }

    public static <T> T[] notEmpty(T[] values) {
        nn(values);
        if (values.length > 0) {
            return values;
        }
        return IAE("array must not be empty");
    }

    public static <T> Set<T> notEmpty(Set<T> values) {
        nn(values);
        if (!values.isEmpty()) {
            return values;
        }
        return IAE("set must not be empty");
    }

    public static String notEmpty(String s) {
        nn(s);
        if (!s.isEmpty()) {
            return s;
        }
        return IAE("string must not be empty");
    }

    public static <T> T nn(T value) {
        if (value != null) {
            return value;
        }
        return IAE("value must not be null");
    }

    public static <T> T[] nn(T[] value) {
        if (value != null) {
            return value;
        }
        return IAE("value must not be null");
    }

    public static <S, T> void nn(S value1, T value2) {
        nn(value1);
        nn(value2);
    }

    public static <S, T, R> void nn(S value1, T value2, R value3) {
        nn(value1);
        nn(value2);
        nn(value3);
    }

    public static <T> T[] nnv(T[] values) {
        return noNullValues(values);
    }

    public static <T> T[] noNullValues(T[] values) {
        nn(values);
        for (T value : values)
            if (Objects.isNull(value))
                return IAE(format("values must be contain null: %s", values));

        return values;
    }

    public static int greaterThanOrEqualZero(int i) {
        if (i >= 0) {
            return i;
        }
        return IAE(format("value %s must be greater than or equal 0.", i));
    }

    public static long greaterThanOrEqualZero(long i) {
        if (i >= 0) {
            return i;
        }
        return IAE(format("value %s must be greater than or equal 0.", i));
    }

	public static double greaterThanOrEqualZero(double d) {
		if (d >= 0d) {
			return d;
		}
		return IAE(format("value %s must be greater than or equal 0.", d));
	}

    public static <T> List<T> noDuplicates(List<T> list) {
        if (list.size() == new HashSet<>(list).size()) {
            return list;
        }
        return IAE("list must not contain duplicates");
    }

    public static int greaterThanZero(int i) {
        return (int) greaterThanZero((long) i);
    }

    public static long greaterThanZero(long i) {
        if (i > 0) {
            return i;
        }
        return IAE(format("value %s must be greater than 0.", i));
    }
    public static double greaterThanZero(double value) {
        return (value > 0.0)
                ?  value
                : IAE(format("value must be greater than 0 but is %s", value));
    }

    public static long lessThan(long value, long upperBound) {
        if (value < upperBound) {
            return value;
        }
        return IAE(format("value must be less than %s but is %s.", upperBound, value));
    }

    public static int lessThan(int value, int upperBound) {
        if (value < upperBound) {
            return value;
        }
        return IAE(format("value must be less than %s but is %s.", upperBound, value));
    }

    public static long greaterThanOrEqual(long value, long lowerBound) {
        if (lowerBound <= value) {
            return value;
        }
        return IAE(format("value must be greater than or equal %s but is %s.", lowerBound, value));
    }

    public static long greaterThanOrEqualAndLessThan(long value, long lowerBound, long upperBound) {
        greaterThanOrEqual(value, lowerBound);
        return lessThan(value, upperBound);
    }

    public static BigInteger greaterThanOrEqualZero(BigInteger value) {
        greaterThanOrEqualZero(nn(value).signum());
        return value;
    }

    public static <X, Y> Map<X, Y> notEmpty(Map<X, Y> map) {
        if (!map.isEmpty())
            return map;
        return IAE("map must not be empty");
    }

    public static String hasLength(int length, String s) {
        nn(s);
        if (s.length() != length) {
            throw new IllegalArgumentException(
                    format("string must have a length '%s' but is '%s' in %s", length, s.length(), s));
        }

        return s;
    }

    public static String noWhitespaces(String s) {
        nn(s);
        String noWhiteSpaces = s.replaceAll("\\s+", "");
        if (!noWhiteSpaces.equals(s)) {
            throw new IllegalArgumentException(format("string must not contain whitespaces: %s", s));
        }

        return s;
    }

    public static double greaterEqualZero(double d) {
        if (d < 0.0) {
            throw new IllegalArgumentException(format("value must not be less than 0.0 but is %s", d));
        }

        return d;
    }
    public static double lessThanOrEqualOne(double value) {
        if (value > 1.0)
            throw new IllegalArgumentException(format("value must be less than 0.0 or equal 0.0 but is %s", value));

        return value;
    }

	public static double lessThanOrEqualZero(double value) {
		if (value > 0.0d)
			throw new IllegalArgumentException(format("value must not be greater than 1.0 but is %s", value));
		return value;
	}

    public static <T> void isNull(T t) {
        if (t != null) {
            throw new IllegalArgumentException("t must be null but is " + t);
        }
    }

    public static <T> List<T> notEmpty(List<T> values) {
        nn(values);
        if (values.isEmpty()) {
            throw new IllegalStateException("values must not be empty");
        }
        return values;
    }

    public static int notZero(int value) {
        if (value == 0) {
            throw new IllegalArgumentException("value must not be zero");
        }
        return value;
    }


    public static double lessThanZero(double value) {
        if (value >= 0.0)
            return IAE(format("values must be less than zero: %s", value));
        return value;
    }
}
