package com.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.util.Assert.assertThrowsIAE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class PreconditionTest {


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


    @Test
    void noWhitespacesShouldAcceptStringWithoutWhitespace() {
        // given
        String input = "hello";

        // when
        String result = Precondition.noWhitespaces(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void noWhitespacesShouldRejectStringWithSpace() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello world"));
    }

    @Test
    void noWhitespacesShouldRejectStringWithTab() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello\tworld"));
    }

    @Test
    void noWhitespacesShouldRejectStringWithNewline() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces("hello\nworld"));
    }

    @Test
    void noWhitespacesShouldRejectNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.noWhitespaces(null));
    }

    @Test
    void upperCaseShouldReturnAlreadyUpperCase() {
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


    @Test
    void uppercaseShouldAcceptAllUpperCaseString() {
        // given
        String input = "HELLO";

        // when
        String result = Precondition.upperCase(input);

        // then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void upperCaseShouldRejectMixedCaseString() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase("Hello"));
    }

    @Test
    void upperCaseShouldRejectLowercaseString() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.upperCase("hello"));
    }

    @Test
    void uppercaseShouldAcceptAllUpperCaseWithNumbers() {
        // given
        String input = "HELLO123";

        // when
        String result = Precondition.upperCase(input);

        // then
        assertThat(result).isEqualTo(input);
    }



    @Test
    void greaterEqualZeroShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.greaterEqualZero(5.5)).isEqualTo(5.5);
    }

    @Test
    void greaterEqualZeroShouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.greaterEqualZero(0.0)).isEqualTo(0.0);
    }

    @Test
    void greaterEqualZeroShouldRejectNegative() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.greaterEqualZero(-0.1));
    }


    @Test
    void lessThanOrEqualOneShouldAcceptValueLessThanOne() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualOne(0.5)).isEqualTo(0.5);
    }

    @Test
    void lessThanOrEqualOneShouldAcceptOne() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualOne(1.0)).isEqualTo(1.0);
    }

    @Test
    void lessThanOrEqualOneShouldRejectValueGreaterThanOne() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanOrEqualOne(1.1));
    }


    @Test
    void lessThanOrEqualZeroShouldAcceptZero() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualZero(0.0)).isEqualTo(0.0);
    }

    @Test
    void lessThanOrEqualZeroShouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.lessThanOrEqualZero(-0.5)).isEqualTo(-0.5);
    }

    @Test
    void lessThanOrEqualZeroShouldRejectPositive() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanOrEqualZero(0.1));
    }


    @Test
    void isNullShouldAcceptNull() {
        // given / when / then (no exception)
        Precondition.isNull(null);
    }

    @Test
    void isNullShouldRejectNonNull() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.isNull("value"));
    }


    @Test
    void notZeroShouldAcceptPositive() {
        // given / when / then
        assertThat(Precondition.notZero(5)).isEqualTo(5);
    }

    @Test
    void notZeroShouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.notZero(-5)).isEqualTo(-5);
    }

    @Test
    void notZeroShouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.notZero(0));
    }


    @Test
    void lessThanZeroShouldAcceptNegative() {
        // given / when / then
        assertThat(Precondition.lessThanZero(-0.5)).isEqualTo(-0.5);
    }

    @Test
    void lessThanZeroShouldRejectZero() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanZero(0.0));
    }

    @Test
    void lessThanZeroShouldRejectPositive() {
        // given / when / then
        assertThrowsIAE(() -> Precondition.lessThanZero(0.1));
    }
}





