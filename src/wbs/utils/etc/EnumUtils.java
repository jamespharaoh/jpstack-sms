package wbs.utils.etc;

import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.camelToSpaces;

import lombok.NonNull;

import wbs.utils.string.LazyString;

public
class EnumUtils {

	public static <Type extends Enum <?>>
	boolean enumEqualSafe (
			@NonNull Type enumValue0,
			@NonNull Type enumValue1) {

		if (enumValue0.getClass () != enumValue1.getClass ()) {
			throw new ClassCastException ();
		}

		return enumValue0 == enumValue1;

	}

	public static <Type extends Enum <?>>
	boolean enumNotEqualSafe (
			@NonNull Type enumValue0,
			@NonNull Type enumValue1) {

		if (enumValue0.getClass () != enumValue1.getClass ()) {
			throw new ClassCastException ();
		}

		return enumValue0 != enumValue1;

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Iterable <Type> examples) {

		for (
			Type example
				: examples
		) {

			if (value.getClass () != example.getClass ()) {
				throw new ClassCastException ();
			}

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	@SuppressWarnings ("unchecked")
	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type ... examples) {

		for (
			Type example
				: examples
		) {

			if (value.getClass () != example.getClass ()) {
				throw new ClassCastException ();
			}

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
			|| value == example4
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
			|| value == example4
			|| value == example5
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5,
			@NonNull Type example6) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example6.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
			|| value == example4
			|| value == example5
			|| value == example6
		);

	}

	public static <Type extends Enum <?>>
	boolean enumInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5,
			@NonNull Type example6,
			@NonNull Type example7) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example6.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example7.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value == example0
			|| value == example1
			|| value == example2
			|| value == example3
			|| value == example4
			|| value == example5
			|| value == example6
			|| value == example7
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Iterable <Type> examples) {

		for (
			Type example
				: examples
		) {

			if (value.getClass () != example.getClass ()) {
				throw new ClassCastException ();
			}

			if (value == example) {
				return false;
			}

		}

		return true;

	}

	@SuppressWarnings ("unchecked")
	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type ... examples) {

		for (
			Type example
				: examples
		) {

			if (value.getClass () != example.getClass ()) {
				throw new ClassCastException ();
			}

			if (value == example) {
				return false;
			}

		}

		return true;

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
			&& value != example4
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
			&& value != example4
			&& value != example5
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5,
			@NonNull Type example6) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example6.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
			&& value != example4
			&& value != example5
			&& value != example6
		);

	}

	public static <Type extends Enum <?>>
	boolean enumNotInSafe (
			@NonNull Type value,
			@NonNull Type example0,
			@NonNull Type example1,
			@NonNull Type example2,
			@NonNull Type example3,
			@NonNull Type example4,
			@NonNull Type example5,
			@NonNull Type example6,
			@NonNull Type example7) {

		if (value.getClass () != example0.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example1.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example2.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example3.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example4.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example5.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example6.getClass ()) {
			throw new ClassCastException ();
		}

		if (value.getClass () != example7.getClass ()) {
			throw new ClassCastException ();
		}

		return (
			value != example0
			&& value != example1
			&& value != example2
			&& value != example3
			&& value != example4
			&& value != example5
			&& value != example6
			&& value != example7
		);

	}

	public static
	String enumName (
			@NonNull Enum <?> enumValue) {

		return enumValue.name ();

	}

	public static
	String enumNameSpaces (
			@NonNull Enum <?> enumValue) {

		return camelToSpaces (
			enumValue.name ());

	}

	public static
	String enumNameHyphens (
			@NonNull Enum <?> enumValue) {

		return camelToHyphen (
			enumValue.name ());

	}

	public static
	LazyString enumNameHyphensLazy (
			@NonNull Enum <?> enumValue) {

		return LazyString.singleton (
			() -> camelToHyphen (
				enumValue.name ()));

	}

}
