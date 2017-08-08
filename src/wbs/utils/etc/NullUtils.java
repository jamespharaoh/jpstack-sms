package wbs.utils.etc;

import static wbs.utils.collection.ArrayUtils.arrayStream;
import static wbs.utils.collection.IterableUtils.iterableStream;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import wbs.framework.logging.TaskLogger;

public
class NullUtils {

	@SafeVarargs
	public static <@Nullable Type>
	Type ifNull (
			Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	@SafeVarargs
	public static <@Nullable Type>
	Type ifNull (
			Supplier <Type> ... valueSuppliers) {

		for (
			Supplier <Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null)
				return value;

		}

		return null;

	}

	public static <Type>
	Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	@SafeVarargs
	public static <Type>
	Type ifNullThenRequired (
			Supplier <? extends Type> ... valueSuppliers) {

		for (
			Supplier <? extends Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null) {
				return value;
			}

		}

		throw new NullPointerException ();

	}

	public static <@Nullable Type>
	Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

	public static
	void errorIfNull (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name,
			Object value) {

		if (
			isNull (
				value)
		) {

			parentTaskLogger.errorFormat (
				"Parameter %s is null",
				name);

		}

	}

	public static <Type>
	Iterable <Type> filterNotNull (
			@NonNull Iterable <Type> iterable) {

		return () ->
			iterableStream (
				iterable)

			.filter (
				item ->
					item != null)

			.iterator ();

	}

	@SafeVarargs
	public static <OutType, InType extends OutType>
	Iterable <@NonNull OutType> filterNotNull (
			InType ... array) {

		return () ->
			arrayStream (
				array)

			.filter (
				item ->
					item != null)

			.map (
				item ->
					(OutType)
					item)

			.iterator ();

	}

	public static <Type>
	List <Type> filterNotNullToList (
			@NonNull Iterable <? extends Type> iterable) {

		return ImmutableList.<Type> copyOf (
			iterableStream (
				iterable)

			.filter (
				item ->
					item != null)

			.iterator ()

		);

	}

	@SuppressWarnings ("unchecked")
	public static <OutType, InType extends OutType>
	List <@NonNull OutType> filterNotNullToList (
			InType ... array) {

		ImmutableList.Builder <@NonNull OutType> builder =
			ImmutableList.builder ();

		for (
			InType item
				: array
		) {

			if (item != null) {

				builder.add (
					item);

			}

		}

		return builder.build ();

	}

	public static
	boolean isNull (
			Object object) {

		return object == null;

	}

	public static
	boolean isNotNull (
			Object object) {

		return object != null;

	}

	public static
	boolean anyIsNotNull (
			Object ... objects) {

		for (
			Object object
				: objects
		) {

			if (object != null) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean allAreNotNull (
			Object ... objects) {

		for (
			Object object
				: objects
		) {

			if (object == null) {
				return false;
			}

		}

		return true;

	}

	public static
	String isNullString (
			Object object) {

		if (object != null) {
			return "not null";
		} else {
			return "null";
		}

	}

}
