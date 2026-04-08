package com.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static com.util.Assert.assertThrowsIAE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class PreconditionTest {

    // --- notEmpty(byte[]) ---

    @Test
    void notEmptyByteArrayShouldAcceptNonEmptyArray() {
        // given
        byte[] input = {1, 2, 3};

        // when
        byte[] result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptyByteArrayShouldRejectEmptyArray() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(new byte[0]));
    }


    // --- notEmpty(T[]) ---

    @Test
    void notEmptyArrayShouldAcceptNonEmptyArray() {
        // given
        String[] input = {"a", "b"};

        // when
        String[] result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptyArrayShouldRejectEmptyArray() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(new String[0]));
    }

    @Test
    void notEmptyArrayShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty((String[]) null));
    }

    // --- notEmpty(Set) ---

    @Test
    void notEmptySetShouldAcceptNonEmptySet() {
        // given
        Set<String> input = new HashSet<>(List.of("a", "b"));

        // when
        Set<String> result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptySetShouldRejectEmptySet() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(new HashSet<>()));
    }

    @Test
    void notEmptySetShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty((Set<String>) null));
    }

    // --- notEmpty(String) ---

    @Test
    void notEmptyStringShouldAcceptNonEmptyString() {
        // given
        String input = "hello";

        // when
        String result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptyStringShouldRejectEmptyString() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(""));
    }

    @Test
    void notEmptyStringShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty((String) null));
    }

    // --- notEmpty(List) ---

    @Test
    void notEmptyListShouldAcceptNonEmptyList() {
        // given
        List<String> input = List.of("a", "b");

        // when
        List<String> result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptyListShouldRejectEmptyList() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(new ArrayList<>()));
    }

    @Test
    void notEmptyListShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty((List<String>) null));
    }

    // --- notEmpty(Map) ---

    @Test
    void notEmptyMapShouldAcceptNonEmptyMap() {
        // given
        Map<String, Integer> input = new HashMap<>();
        input.put("key", 1);

        // when
        Map<String, Integer> result = Precondition.notEmpty(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void notEmptyMapShouldRejectEmptyMap() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notEmpty(new HashMap<>()));
    }

    // --- nn(T) ---

    @Test
    void nnObjectShouldAcceptNonNullValue() {
        // given
        String input = "hello";

        // when
        String result = Precondition.nn(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void nnObjectShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn(null));
    }

    // --- nn(T[]) ---

    @Test
    void nnArrayShouldAcceptNonNullArray() {
        // given
        String[] input = {"a"};

        // when
        String[] result = Precondition.nn(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void nnArrayShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn((String[]) null));
    }

    // --- nn(S, T) ---

    @Test
    void nnTwoObjectsShouldAcceptBothNonNull() {
        // given / when / then (no exception)
        Precondition.nn("a", "b");
    }

    @Test
    void nnTwoObjectsShouldRejectFirstNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn(null, "b"));
    }

    @Test
    void nnTwoObjectsShouldRejectSecondNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn("a", null));
    }

    // --- nn(S, T, R) ---

    @Test
    void nnThreeObjectsShouldAcceptAllNonNull() {
        // given / when / then (no exception)
        Precondition.nn("a", "b", "c");
    }

    @Test
    void nnThreeObjectsShouldRejectFirstNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn(null, "b", "c"));
    }

    @Test
    void nnThreeObjectsShouldRejectSecondNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn("a", null, "c"));
    }

    @Test
    void nnThreeObjectsShouldRejectThirdNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.nn("a", "b", null));
    }

    // --- nnv / noNullValues ---

    @Test
    void noNullValuesShouldAcceptArrayWithoutNulls() {
        // given
        String[] input = {"a", "b", "c"};

        // when
        String[] result = Precondition.noNullValues(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void noNullValuesShouldRejectArrayWithNull() {
        // given
        String[] input = {"a", null, "c"};

        // when / then
        assertThrowsIAE(() -> Precondition.noNullValues(input));
    }

    @Test
    void noNullValuesShouldRejectNullArray() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noNullValues(null));
    }

    @Test
    void nnvShouldCallNoNullValues() {
        // given
        String[] input = {"a", "b"};

        // when
        String[] result = Precondition.nnv(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    // --- greaterThanOrEqualZero(int) ---

    @Test
    void greaterThanOrEqualZeroIntShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(5)).isEqualTo(5);
    }

    @Test
    void greaterThanOrEqualZeroIntShouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(0)).isEqualTo(0);
    }

    @Test
    void greaterThanOrEqualZeroIntShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualZero(-1));
    }

    // --- greaterThanOrEqualZero(long) ---

    @Test
    void greaterThanOrEqualZeroLongShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(5L)).isEqualTo(5L);
    }

    @Test
    void greaterThanOrEqualZeroLongShouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(0L)).isEqualTo(0L);
    }

    @Test
    void greaterThanOrEqualZeroLongShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualZero(-1L));
    }

    // --- greaterThanOrEqualZero(double) ---

    @Test
    void greaterThanOrEqualZeroDoubleShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(5.5)).isEqualTo(5.5);
    }

    @Test
    void greaterThanOrEqualZeroDoubleShouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualZero(0.0)).isEqualTo(0.0);
    }

    @Test
    void greaterThanOrEqualZeroDoubleShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualZero(-0.1));
    }

    // --- greaterThanOrEqualZero(BigInteger) ---

    @Test
    void greaterThanOrEqualZeroBigIntegerShouldAcceptPositive() {
        // given
        BigInteger input = new BigInteger("5");

        // when
        BigInteger result = Precondition.greaterThanOrEqualZero(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void greaterThanOrEqualZeroBigIntegerShouldAcceptZero() {
        // given
        BigInteger input = BigInteger.ZERO;

        // when
        BigInteger result = Precondition.greaterThanOrEqualZero(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void greaterThanOrEqualZeroBigIntegerShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualZero(new BigInteger("-1")));
    }

    @Test
    void greaterThanOrEqualZeroBigIntegerShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualZero(null));
    }

    // --- noDuplicates ---

    @Test
    void noDuplicatesShouldAcceptListWithoutDuplicates() {
        // given
        List<String> input = List.of("a", "b", "c");

        // when
        List<String> result = Precondition.noDuplicates(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void noDuplicatesShouldRejectListWithDuplicates() {
        // given
        List<String> input = new ArrayList<>(List.of("a", "b", "a"));

        // when / then
        assertThrowsIAE(() -> Precondition.noDuplicates(input));
    }

    // --- greaterThanZero(int) ---

    @Test
    void greaterThanZeroIntShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanZero(5)).isEqualTo(5);
    }

    @Test
    void greaterThanZeroIntShouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(0));
    }

    @Test
    void greaterThanZeroIntShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(-1));
    }

    // --- greaterThanZero(long) ---

    @Test
    void greaterThanZeroLongShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanZero(5L)).isEqualTo(5L);
    }

    @Test
    void greaterThanZeroLongShouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(0L));
    }

    @Test
    void greaterThanZeroLongShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(-1L));
    }

    // --- greaterThanZero(double) ---

    @Test
    void greaterThanZeroDoubleShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterThanZero(0.1)).isEqualTo(0.1);
    }

    @Test
    void greaterThanZeroDoubleShouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(0.0));
    }

    @Test
    void greaterThanZeroDoubleShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanZero(-0.1));
    }

    // --- lessThan(long, long) ---

    @Test
    void lessThanLongShouldAcceptValueLessThanBound() {
        // given / when / then
        assertThat(Precondition.lessThan(5L, 10L)).isEqualTo(5L);
    }

    @Test
    void lessThanLongShouldRejectValueEqualToBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThan(10L, 10L));
    }

    @Test
    void lessThanLongShouldRejectValueGreaterThanBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThan(15L, 10L));
    }

    // --- lessThan(int, int) ---

    @Test
    void lessThanIntShouldAcceptValueLessThanBound() {
        // given / when / then
        assertThat(Precondition.lessThan(5, 10)).isEqualTo(5);
    }

    @Test
    void lessThanIntShouldRejectValueEqualToBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThan(10, 10));
    }

    @Test
    void lessThanIntShouldRejectValueGreaterThanBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThan(15, 10));
    }

    // --- greaterThanOrEqual ---

    @Test
    void greaterThanOrEqualShouldAcceptValueGreaterThanBound() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqual(15L, 10L)).isEqualTo(15L);
    }

    @Test
    void greaterThanOrEqualShouldAcceptValueEqualToBound() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqual(10L, 10L)).isEqualTo(10L);
    }

    @Test
    void greaterThanOrEqualShouldRejectValueLessThanBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqual(5L, 10L));
    }

    // --- greaterThanOrEqualAndLessThan ---

    @Test
    void greaterThanOrEqualAndLessThanShouldAcceptValueInRange() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualAndLessThan(5L, 0L, 10L)).isEqualTo(5L);
    }

    @Test
    void greaterThanOrEqualAndLessThanShouldAcceptLowerBound() {
        // given / when / then
        assertThat(Precondition.greaterThanOrEqualAndLessThan(0L, 0L, 10L)).isEqualTo(0L);
    }

    @Test
    void greaterThanOrEqualAndLessThanShouldRejectUpperBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualAndLessThan(10L, 0L, 10L));
    }

    @Test
    void greaterThanOrEqualAndLessThanShouldRejectValueBelowLowerBound() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterThanOrEqualAndLessThan(-1L, 0L, 10L));
    }

    // --- hasLength ---

    @Test
    void hasLengthShouldAcceptStringWithCorrectLength() {
        // given
        String input = "hello";

        // when
        String result = Precondition.hasLength(5, input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void hasLengthShouldRejectStringWithIncorrectLength() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.hasLength(5, "hi"));
    }

    @Test
    void hasLengthShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.hasLength(5, null));
    }

    // --- noWhitespaces ---

    @Test
    void noWhitespaces_shouldAcceptStringWithoutWhitespace() {
        // given
        String input = "hello";

        // when
        String result = Precondition.noWhitespaces(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void noWhitespaces_shouldRejectStringWithSpace() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello world"));
    }

    @Test
    void noWhitespaces_shouldRejectStringWithTab() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello\tworld"));
    }

    @Test
    void noWhitespaces_shouldRejectStringWithNewline() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello\nworld"));
    }

    @Test
    void noWhitespaces_shouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces(null));
    }

    @Test
    void upperCase_shouldReturnAlreadyUpperCase() {
        // given
        String input = "HELLO";

        // when
        String result = Precondition.upperCase(input);

        // then
        assertThat(result).isEqualTo("HELLO");
    }

    @Test
    void upperCaseShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase(null));
    }

    // --- uppercase ---

    @Test
    void uppercase_shouldAcceptAllUpperCaseString() {
        // given
        String input = "HELLO";

        // when
        String result = Precondition.upperCase(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void upperCase_shouldRejectMixedCaseString() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase("Hello"));
    }

    @Test
    void upperCase_shouldRejectLowercaseString() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase("hello"));
    }

    @Test
    void uppercase_shouldAcceptAllUpperCaseWithNumbers() {
        // given
        String input = "HELLO123";

        // when
        String result = Precondition.upperCase(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void upperCase_shouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase(null));
    }

    // --- greaterEqualZero ---

    @Test
    void greaterEqualZero_shouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterEqualZero(5.5)).isEqualTo(5.5);
    }

    @Test
    void greaterEqualZero_shouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.greaterEqualZero(0.0)).isEqualTo(0.0);
    }

    @Test
    void greaterEqualZero_shouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterEqualZero(-0.1));
    }

    // --- lessThanOrEqualOne ---

    @Test
    void lessThanOrEqualOne_shouldAcceptValueLessThanOne() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualOne(0.5)).isEqualTo(0.5);
    }

    @Test
    void lessThanOrEqualOne_shouldAcceptOne() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualOne(1.0)).isEqualTo(1.0);
    }

    @Test
    void lessThanOrEqualOne_shouldRejectValueGreaterThanOne() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanOrEqualOne(1.1));
    }

    // --- lessThanOrEqualZero ---

    @Test
    void lessThanOrEqualZero_shouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualZero(0.0)).isEqualTo(0.0);
    }

    @Test
    void lessThanOrEqualZero_shouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualZero(-0.5)).isEqualTo(-0.5);
    }

    @Test
    void lessThanOrEqualZero_shouldRejectPositive() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanOrEqualZero(0.1));
    }

    // --- isNull ---

    @Test
    void isNull_shouldAcceptNull() {
        // given / when / then (no exception)
        Precondition.isNull(null);
    }

    @Test
    void isNull_shouldRejectNonNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.isNull("value"));
    }

    // --- notZero ---

    @Test
    void notZero_shouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.notZero(5)).isEqualTo(5);
    }

    @Test
    void notZero_shouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.notZero(-5)).isEqualTo(-5);
    }

    @Test
    void notZero_shouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notZero(0));
    }

    // --- lessThanZero ---

    @Test
    void lessThanZero_shouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.lessThanZero(-0.5)).isEqualTo(-0.5);
    }

    @Test
    void lessThanZero_shouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanZero(0.0));
    }

    @Test
    void lessThanZero_shouldRejectPositive() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanZero(0.1));
    }
}





