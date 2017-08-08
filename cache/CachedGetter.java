package wbs.utils.cache;

public
interface CachedGetter <Context, Type> {

	Type get (
			Context context);

}
