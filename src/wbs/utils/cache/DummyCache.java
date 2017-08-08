package wbs.utils.cache;

import java.util.function.BiFunction;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class DummyCache <Context, Key, Value>
	implements AdvancedCache <Context, Key, Value> {

	// properties

	@Getter @Setter
	BiFunction <Context, Key, Optional <Value>> lookupByKeyFunction;

	// implementation

	@Override
	public
	Optional <Value> find (
			@NonNull Context context,
			@NonNull Key key) {

		return lookupByKeyFunction.apply (
			context,
			key);

	}

	@Override
	public
	Value create (
			@NonNull Context parentTaskLogger,
			@NonNull Key key) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Value findOrCreate (
			@NonNull Context parentTaskLogger,
			@NonNull Key key) {

		throw new UnsupportedOperationException ();

	}

}
