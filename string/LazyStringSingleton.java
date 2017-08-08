package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.List;
import java.util.function.Supplier;

import lombok.NonNull;

public
class LazyStringSingleton
	implements LazyString {

	// state

	private
	Supplier <String> valueSupplier;

	private
	String value;

	// constructor

	public
	LazyStringSingleton (
			@NonNull Supplier <String> valueSupplier) {

		this.valueSupplier =
			valueSupplier;

	}

	// lazy string implementation

	@Override
	public
	List <String> toParts () {

		return singletonList (
			toString ());

	}

	// object implementation

	@Override
	public
	String toString () {

		if (
			isNull (
				value)
		) {

			value =
				requiredValue (
					valueSupplier.get ());

			valueSupplier =
				null;

		}

		return value;

	}

}
