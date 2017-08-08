package wbs.utils.etc;

import static wbs.utils.collection.IterableUtils.iterableMap;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.utils.exception.RuntimeNumberFormatException;
import wbs.utils.string.LazyString;

public
class NumberUtils {

	public final static
	Long maximumInteger =
		Long.MAX_VALUE;

	public final static
	Long minimumInteger =
		Long.MIN_VALUE;

	public final static
	Long maximumJavaInteger =
		fromJavaInteger (
			Integer.MAX_VALUE);

	public final static
	Long minimumJavaInteger =
		fromJavaInteger (
			Integer.MIN_VALUE);

	// ---------- string

	public static
	Long parseIntegerRequired (
			@NonNull String stringValue) {

		try {

			return Long.parseLong (
				stringValue);

		} catch (NumberFormatException numberFormatException) {

			throw new RuntimeNumberFormatException (
				numberFormatException);

		}

	}

	public static
	Optional <Long> parseInteger (
			@NonNull String stringValue) {

		try {

			return Optional.of (
				Long.parseLong (
					stringValue));

		} catch (NumberFormatException numberFormatException) {

			return Optional.absent ();

		}

	}

	public static
	String integerToDecimalString (
			@NonNull Long integerValue) {

		return integerValue.toString ();

	}

	public static
	Iterable <String> integerToDecimalString (
			@NonNull Iterable <Long> integerValues) {

		return iterableMap (
			integerValues,
			integerValue ->
				integerValue.toString ());

	}

	public static
	LazyString integerToDecimalStringLazy (
			@NonNull Long integerValue) {

		return LazyString.singleton (
			() -> integerToDecimalString (
				integerValue));

	}

	public static
	String integerToDecimalString (
			@NonNull Integer integerValue) {

		return integerValue.toString ();

	}

	public static
	LazyString integerToDecimalStringLazy (
			@NonNull Integer integerValue) {

		return LazyString.singleton (
			() -> integerToDecimalString (
				integerValue));

	}

	public static
	String floatToDecimalString (
			@NonNull Float floatValue) {

		return floatValue.toString ();

	}

	public static
	String floatToDecimalString (
			@NonNull Double floatValue) {

		return floatValue.toString ();

	}

	// ---------- java integer (32 bit)

	public static
	Optional <Integer> toJavaInteger (
			@NonNull Long integerValue) {

		try {

			return Optional.of (
				Math.toIntExact (
					integerValue));

		} catch (ArithmeticException arithmeticException) {

			return Optional.absent ();

		}

	}

	public static
	int toJavaIntegerRequired (
			@NonNull Long integerValue) {

		return Math.toIntExact (
			integerValue);

	}

	public static
	int toJavaIntegerRequired (
			long integerValue) {

		return Math.toIntExact (
			integerValue);

	}

	public static
	Long fromJavaInteger (
			@NonNull Integer javaIntegerObject) {

		int javaIntegerValue =
			javaIntegerObject;

		long integerValue =
			javaIntegerValue;

		return integerValue;

	}

	public static
	long fromJavaInteger (
			int javaIntegerValue) {

		long integerValue =
			javaIntegerValue;

		return integerValue;

	}

	// ---------- floating point

	public static
	long roundToIntegerRequired (
			double originalDoubleValue) {

		double roundedDoubleValue =
			Math.round (
				originalDoubleValue);

		if (
			roundedDoubleValue < Long.MIN_VALUE
			|| roundedDoubleValue > Long.MAX_VALUE
		) {
			throw new ArithmeticException ();
		}

		return (long)
			roundedDoubleValue;

	}

	// ---------- equality

	public static
	boolean integerEqualSafe (
			long integer0,
			long integer1) {

		return integer0 == integer1;

	}

	public static
	boolean integerNotEqualSafe (
			long integer0,
			long integer1) {

		return integer0 != integer1;

	}

	public static
	boolean integerInSafe (
			long value,
			long... examples) {

		for (
			long example
				: examples
		) {

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean integerInSafe (
			long value,
			long example0) {

		return value == example0;

	}

	public static
	boolean integerInSafe (
			long value,
			long example0,
			long example1) {

		return (
			value == example0
			|| value == example1
		);

	}

	public static
	boolean integerInSafe (
			long value,
			long example0,
			long example1,
			long example2) {

		return (
			value == example0
			|| value == example1
			|| value == example2
		);

	}

	public static
	boolean integerInSafe (
			long value,
			long example0,
			long example1,
			long example2,
			long example3) {

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
		);

	}

	public static
	boolean integerInSafe (
			long value,
			long example0,
			long example1,
			long example2,
			long example3,
			long example4) {

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
			|| value == example4
		);

	}

	public static
	boolean integerNotInSafe (
			long value,
			long... examples) {

		for (
			long example
				: examples
		) {

			if (value == example) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean integerNotInSafe (
			long value,
			long example0) {

		return value != example0;

	}

	public static
	boolean integerNotInSafe (
			long value,
			long example0,
			long example1) {

		return (
			value != example0
			&& value != example1
		);

	}

	public static
	boolean integerNotInSafe (
			long value,
			long example0,
			long example1,
			long example2) {

		return (
			value != example0
			&& value != example1
			&& value != example2
		);

	}

	public static
	boolean integerNotInSafe (
			long value,
			long example0,
			long example1,
			long example2,
			long example3) {

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
		);

	}

	public static
	boolean integerNotInSafe (
			long value,
			long example0,
			long example1,
			long example2,
			long example3,
			long example4) {

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
			&& value != example4
		);

	}

	// ---------- less than

	public static
	boolean notLessThan (
			int left,
			int right) {

		return left >= right;

	}

	public static
	boolean notLessThan (
			long left,
			long right) {

		return left >= right;

	}

	public static
	boolean moreThan (
			int left,
			int right) {

		return left > right;

	}

	public static
	boolean moreThan (
			long left,
			long right) {

		return left > right;

	}

	public static
	boolean notMoreThan (
			int left,
			int right) {

		return left <= right;

	}

	public static
	boolean notMoreThan (
			long left,
			long right) {

		return left <= right;

	}

	// ---------- zero

	public static
	boolean equalToZero (
			long value) {

		return value == 0l;

	}

	public static
	boolean notEqualToZero (
			long value) {

		return value != 0l;

	}

	public static
	boolean lessThanZero (
			long value) {

		return value < 0l;

	}

	public static
	boolean notLessThanZero (
			long value) {

		return value >= 0l;

	}

	public static
	boolean moreThanZero (
			long value) {

		return value > 0l;

	}

	public static
	boolean notMoreThanZero (
			long value) {

		return value <= 0l;

	}

	// ---------- one

	public static
	boolean equalToOne (
			long value) {

		return value == 1l;

	}

	public static
	boolean notEqualToOne (
			long value) {

		return value != 1l;

	}

	public static
	boolean lessThanOne (
			long value) {

		return value < 1l;

	}

	public static
	boolean notLessThanOne (
			long value) {

		return value >= 1l;

	}

	public static
	boolean moreThanOne (
			long value) {

		return value > 1l;

	}

	public static
	boolean notMoreThanOne (
			long value) {

		return value <= 1l;

	}

	// ---------- two

	public static
	boolean equalToTwo (
			long value) {

		return value == 2l;

	}

	public static
	boolean notEqualToTwo (
			long value) {

		return value != 2l;

	}

	// ---------- three

	public static
	boolean equalToThree (
			long value) {

		return value == 3l;

	}

	// ---------- four

	public static
	boolean equalToFour (
			long value) {

		return value == 4l;

	}

	// ---------- sets

	public static
	Set <Long> integerRangeAsSet (
			long start,
			long end) {

		return ImmutableSet.copyOf (
			LongStream

			.range (
				200l,
				300l)

			.mapToObj (
				value ->
					(Long)
					value)

			.collect (
				Collectors.toSet ())

		);

	}

}
