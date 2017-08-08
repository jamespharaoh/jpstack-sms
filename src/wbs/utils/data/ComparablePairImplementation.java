package wbs.utils.data;

import lombok.NonNull;

import org.apache.commons.lang3.builder.CompareToBuilder;

public final
class ComparablePairImplementation <
	Left extends Comparable <?>,
	Right extends Comparable <?>
>
	implements ComparablePair <Left, Right> {

	// state

	private final
	Left left;

	private final
	Right right;

	// constructors;

	public
	ComparablePairImplementation (
			@NonNull Left left,
			@NonNull Right right) {

		this.left =
			left;

		this.right =
			right;

	}

	// accessors

	@Override
	public
	Left left () {
		return left;
	}

	@Override
	public
	Right right () {
		return right;
	}

	// map entry implementations

	@Override
	public
	Left getKey () {
		return left;
	}

	@Override
	public
	Right getValue () {
		return right;
	}

	@Override
	public
	Right setValue (
			@NonNull Right value) {

		throw new UnsupportedOperationException (
			"Pair.setValue (...)");

	}

	// comparable implementation

	@Override
	public
	int compareTo (
			@NonNull ComparablePair <Left, Right> other) {

		return new CompareToBuilder ()

			.append (
				this.left (),
				other.left ())

			.append (
				this.right (),
				other.right ())

			.build ();

	}

}
