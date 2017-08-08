package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringLength;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class LazyStringList
	implements LazyString {

	// state

	private
	Supplier <List <String>> valuesSupplier;

	private
	Optional <List <String>> values =
		optionalAbsent ();

	private
	Optional <String> value =
		optionalAbsent ();

	private
	Optional <Long> length =
		optionalAbsent ();

	// constructors

	public
	LazyStringList (
			@NonNull Supplier <List <String>> valuesSupplier) {

		this.valuesSupplier =
			valuesSupplier;

	}

	public
	LazyStringList (
			@NonNull List <LazyString> lazyStrings) {

		this (
			() ->
				lazyStrings.stream ()

			.flatMap (
				lazyString ->
					lazyString.toParts ().stream ())

			.collect (
				Collectors.toList ())

		);

	}

	// lazy string implementation

	@Override
	public
	List <String> toParts () {

		if (
			optionalIsNotPresent (
				values)
		) {

			values =
				optionalOf (
					valuesSupplier.get ());

			valuesSupplier = null;

		}

		return optionalGetRequired (
			values);

	}

	// char sequence implementation

	@Override
	public
	int length () {

		if (
			optionalIsPresent (
				length)
		) {

			// do nothing

		} else if (
			optionalIsPresent (
				value)
		) {

			// get length from value

			length =
				optionalOf (
					stringLength (
						optionalGetRequired (
							value)));

		} else {

			// get length from parts

			length =
				optionalOf (
					toParts ().stream ()

				.mapToLong (
					part ->
						(long) part.length ())

				.sum ()

			);

		}

		// return

		return toJavaIntegerRequired (
			optionalGetRequired (
				length));

	}

	@Override
	public
	char charAt (
			int index) {

		return toString ().charAt (
			index);


	}

	@Override
	public
	CharSequence subSequence (
			int start,
			int end) {

		return toString ().subSequence (
			start,
			end);

	}

	@Override
	public
	String toString () {

		if (
			optionalIsPresent (
				value)
		) {

			String valueTemp =
				joinWithoutSeparator (
					toParts ());

			value =
				optionalOf (
					valueTemp);

			values =
				optionalOf (
					singletonList (
						valueTemp));

		}

		return optionalGetRequired (
			value);

	}

}
