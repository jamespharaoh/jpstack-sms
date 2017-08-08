package wbs.utils.cache;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("advancedCache")
public
interface AdvancedCache <Context, Key, Value> {

	Optional <Value> find (
			Context context,
			Key key);

	Value findOrCreate (
			Context context,
			Key key);

	Value create (
			Context context,
			Key key);

}
