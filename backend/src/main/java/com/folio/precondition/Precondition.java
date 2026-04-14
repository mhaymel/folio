package com.folio.precondition;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.folio.precondition.Throw.illegalArgument;

public final class Precondition {


    public static byte[] notEmpty(byte[] bytes) {
        return (nn(bytes).length > 0)
                ? bytes
                : illegalArgument("byte array must not be empty");
    }

    public static <T> T[] notEmpty(T[] values) {
        return (nn(values).length > 0)
                ? values
                : illegalArgument("array must not be empty");
    }

    public static <T> Set<T> notEmpty(Set<T> values) {
        return (!nn(values).isEmpty())
                ? values
                : illegalArgument("set must not be empty");
    }

    public static String notEmpty(String value) {
        return (!nn(value).isEmpty())
                ? value
                : illegalArgument("string must not be empty");
    }

    public static <T> T nn(T value) {
        return (value != null)
                ? value
                : illegalArgument("value must not be null");
    }

    public static <T> T[] nn(T[] value) {
        return (value != null)
                ? value
                : illegalArgument("value must not be null");
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
        for (T value : nn(values))
            if (Objects.isNull(value))
                return illegalArgument("values must be contain null: %s", values);

        return values;
    }

    public static int greaterThanOrEqualZero(int value) {
        return (value >= 0)
                ? value
                : illegalArgument("value %s must be greater than or equal 0.", value);
    }

    public static long greaterThanOrEqualZero(long value) {
        return (value >= 0)
                ? value
                : illegalArgument("value %s must be greater than or equal 0.", value);
    }

    public static double greaterThanOrEqualZero(double value) {
        return (value >= 0d)
                ? value
                : illegalArgument("value %s must be greater than or equal 0.", value);
    }

    public static <T> List<T> noDuplicates(List<T> list) {
        return (list.size() == new HashSet<>(list).size())
                ? list
                : illegalArgument("list must not contain duplicates");
    }

    public static int greaterThanZero(int value) {
        return (int) greaterThanZero((long) value);
    }

    public static long greaterThanZero(long value) {
        return (value > 0)
                ? value
                : illegalArgument("value %s must be greater than 0.", value);
    }
    public static double greaterThanZero(double value) {
        return (value > 0.0)
                ?  value
                : illegalArgument("value must be greater than 0 but is %s", value);
    }

    public static long lessThan(long value, long upperBound) {
        return (value < upperBound)
                ? value
                : illegalArgument("value must be less than %s but is %s.", upperBound, value);
    }

    public static int lessThan(int value, int upperBound) {
        return (value < upperBound)
                ? value
                : illegalArgument("value must be less than %s but is %s.", upperBound, value);
    }

    public static long greaterThanOrEqual(long value, long lowerBound) {
        return (lowerBound <= value)
                ? value
                : illegalArgument("value must be greater than or equal %s but is %s.", lowerBound, value);
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
        return (!nn(map).isEmpty())
                ? map
                : illegalArgument("map must not be empty");
    }

    public static String hasLength(int length, String value) {
        return (nn(value).length() == length)
                ? value
                : illegalArgument("string must have a length '%s' but is '%s' in %s", length, value.length(), value);
    }

    public static String noWhitespaces(String value) {
        String noWhiteSpaces = nn(value).replaceAll("\\s+", "");
        return noWhiteSpaces.equals(value)
                ? value
                : illegalArgument("string must not contain whitespaces: %s", value);
    }

    public static String upperCase(String value) {
        return nn(value).toUpperCase().equals(value)
                ? value
                : illegalArgument("string must be all uppercase but is %s", value);
    }

    public static double greaterEqualZero(double value) {
        return (value >= 0.0)
                ? value
                : illegalArgument("value must not be less than 0.0 but is %s", value);
    }
    public static double lessThanOrEqualOne(double value) {
        return (value <= 1.0)
                ? value
                : illegalArgument("value must be less than 0.0 or equal 0.0 but is %s", value);
    }

    public static double lessThanOrEqualZero(double value) {
        return (value <= 0.0d)
                ? value
                : illegalArgument("value must not be greater than 1.0 but is %s", value);
    }

    public static <T> void isNull(T value) {
        if (value != null)
            illegalArgument("value must be null but is " + value);
    }

    public static <T> List<T> notEmpty(List<T> values) {
        return (!nn(values).isEmpty())
                ? values
                : illegalArgument("list must not be empty");
    }

    public static int notZero(int value) {
        return (value != 0)
                ? value
                : illegalArgument("value must not be zero");
    }


    public static double lessThanZero(double value) {
        return (value < 0.0)
                ? value
                : illegalArgument("values must be less than zero: %s", value);
    }
}
