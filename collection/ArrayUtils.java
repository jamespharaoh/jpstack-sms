package wbs.utils.collection;

import static wbs.utils.etc.NullUtils.isNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.NonNull;

public
class ArrayUtils {

	public static
	boolean arrayIsEmpty (
			@NonNull Object[] array) {

		return array.length == 0;

	}

	public static
	boolean arrayIsNotEmpty (
			@NonNull Object[] array) {

		return array.length > 0;

	}

	@SuppressWarnings ("unchecked")
	public static <InputType, OutputType>
	List <OutputType> arrayMap (
			@NonNull Function <? super InputType, ? extends OutputType> mapFunction,
			@NonNull InputType ... input) {

		List <OutputType> output =
			new ArrayList<> ();

		for (
			InputType inputItem
				: input
		) {

			output.add (
				mapFunction.apply (
					inputItem));

		}

		return output;

	}

	public static <Type>
	Type arrayFirstItemRequired (
			@NonNull Type [] array) {

		if (array.length < 1) {
			throw new NoSuchElementException ();
		}

		Type item =
			array [0];

		if (
			isNull (
				item)
		) {
			throw new NullPointerException ();
		}

		return item;

	}

	public static <Type>
	Iterable <Type> arrayFilter (
			@NonNull Type[] array,
			@NonNull Predicate <Type> predicate) {

		return () ->
			Arrays.stream (
				array)

			.filter (
				predicate)

			.iterator ();

	}

	public static <Type>
	Stream <Type> arrayStream (
			@NonNull Type[] array) {

		return Arrays.stream (
			array);

	}

	public static <Type>
	boolean arrayHasOneItem (
			@NonNull Type[] array) {

		return array.length == 1;

	}

}
