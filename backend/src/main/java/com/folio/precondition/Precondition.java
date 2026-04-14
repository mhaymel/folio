package com.folio.precondition;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.folio.precondition.Throw.IAE;

public final class Precondition {


    public static byte[] notEmpty(byte[] bytes) {
        return (nn(bytes).length > 0)
                ? bytes
                : IAE("byte array must not be empty");
    }

    public static <T> T[] notEmpty(T[] values) {
        return (nn(values).length > 0)
                ? values
                : IAE("array must not be empty");
    }

    public static <T> Set<T> notEmpty(Set<T> values) {
        return (!nn(values).isEmpty())
                ? values
                : IAE("set must not be empty");
    }

    public static String notEmpty(String s) {
        return (!nn(s).isEmpty())
                ? s
                : IAE("string must not be empty");
    }

    public static <T> T nn(T value) {
        return (value != null)
                ? value
                : IAE("value must not be null");
    }

    public static <T> T[] nn(T[] value) {
        return (value != null)
                ? value
                : IAE("value must not be null");
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
                return IAE("values must be contain null: %s", values);

        return values;
    }

    public static int greaterThanOrEqualZero(int i) {
        return (i >= 0)
                ? i
                : IAE("value %s must be greater than or equal 0.", i);
    }

    public static long greaterThanOrEqualZero(long i) {
        return (i >= 0)
                ? i
                : IAE("value %s must be greater than or equal 0.", i);
    }

    public static double greaterThanOrEqualZero(double d) {
        return (d >= 0d)
                ? d
                : IAE("value %s must be greater than or equal 0.", d);
    }

    public static <T> List<T> noDuplicates(List<T> list) {
        return (list.size() == new HashSet<>(list).size())
                ? list
                : IAE("list must not contain duplicates");
    }

    public static int greaterThanZero(int i) {
        return (int) greaterThanZero((long) i);
    }

    public static long greaterThanZero(long i) {
        return (i > 0)
                ? i
                : IAE("value %s must be greater than 0.", i);
    }
    public static double greaterThanZero(double value) {
        return (value > 0.0)
                ?  value
                : IAE("value must be greater than 0 but is %s", value);
    }

    public static long lessThan(long value, long upperBound) {
        return (value < upperBound)
                ? value
                : IAE("value must be less than %s but is %s.", upperBound, value);
    }

    public static int lessThan(int value, int upperBound) {
        return (value < upperBound)
                ? value
                : IAE("value must be less than %s but is %s.", upperBound, value);
    }

    public static long greaterThanOrEqual(long value, long lowerBound) {
        return (lowerBound <= value)
                ? value
                : IAE("value must be greater than or equal %s but is %s.", lowerBound, value);
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
                : IAE("map must not be empty");
    }

    public static String hasLength(int length, String s) {
        return (nn(s).length() == length)
                ? s
                : IAE("string must have a length '%s' but is '%s' in %s", length, s.length(), s);
    }

    public static String noWhitespaces(String s) {
        String noWhiteSpaces = nn(s).replaceAll("\\s+", "");
        return noWhiteSpaces.equals(s)
                ? s
                : IAE("string must not contain whitespaces: %s", s);
    }

    public static String upperCase(String s) {
        return nn(s).toUpperCase().equals(s)
                ? s
                : IAE("string must be all uppercase but is %s", s);
    }

    public static double greaterEqualZero(double d) {
        return (d >= 0.0)
                ? d
                : IAE("value must not be less than 0.0 but is %s", d);
    }
    public static double lessThanOrEqualOne(double value) {
        return (value <= 1.0)
                ? value
                : IAE("value must be less than 0.0 or equal 0.0 but is %s", value);
    }

    public static double lessThanOrEqualZero(double value) {
        return (value <= 0.0d)
                ? value
                : IAE("value must not be greater than 1.0 but is %s", value);
    }

    public static <T> void isNull(T t) {
        if (t != null)
            IAE("t must be null but is " + t);
    }

    public static <T> List<T> notEmpty(List<T> values) {
        return (!nn(values).isEmpty())
                ? values
                : IAE("list must not be empty");
    }

    public static int notZero(int value) {
        return (value != 0)
                ? value
                : IAE("value must not be zero");
    }


    public static double lessThanZero(double value) {
        return (value < 0.0)
                ? value
                : IAE("values must be less than zero: %s", value);
    }
}
