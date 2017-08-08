package wbs.framework.entity.meta.cachedview;

import wbs.framework.entity.meta.model.ModelDataSpec;

public
interface CachedAggregateFieldSpec
	extends ModelDataSpec {

	String name ();

	String fieldName ();

	String when ();

}
