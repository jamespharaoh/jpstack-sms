package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class LazyFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString;

	@Getter @Setter
	long indentSize = 0l;

	// state

	Boolean open;

	List <LazyString> lazyParts;
	List <String> stringParts;

	// constructors

	public
	LazyFormatWriter () {

		open = true;

		lazyParts =
			new ArrayList<> ();

	}

	// public implementation

	public
	List <String> stringParts () {

		if (open) {

			stringParts =
				lazyParts.stream ()

				.flatMap (
					lazyPart ->
						lazyPart.toParts ().stream ())

				.collect (
					Collectors.toList ())

			;

			lazyParts =
				null;

			open = false;

		}

		return stringParts;

	}

	// format writer implementation

	@Override
	public
	void writeString (
			@NonNull LazyString lazyString) {

		if (! open) {
			throw new IllegalStateException ();
		}

		lazyParts.add (
			lazyString);

	}

	@Override
	public
	void close () {

		open =
			false;

	}

	// object implementation

	@Override
	public
	String toString () {

		if (
			collectionDoesNotHaveOneElement (
				stringParts ())
		) {

			this.stringParts =
				singletonList (
					joinWithoutSeparator (
						stringParts));

		}

		return iterableOnlyItemRequired (
			stringParts);

	}

}
