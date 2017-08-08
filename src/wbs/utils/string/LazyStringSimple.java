package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.singletonList;

import java.util.List;

import lombok.NonNull;

public
class LazyStringSimple
	implements LazyString {

	// state

	private final
	String value;

	// constructors

	public
	LazyStringSimple (
			@NonNull String value) {

		this.value =
			value;

	}

	// lazy string implementation

	@Override
	public
	List <String> toParts () {

		return singletonList (
			value);

	}

	// object implementation

	@Override
	public
	String toString () {

		return value;

	}

}
