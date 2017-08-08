package wbs.framework.entity.meta.cachedview;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = { "cachedView", "name" })
@ToString (of = { "name" })
@DataClass ("count-field")
@PrototypeComponent ("cachedCountFieldSpec")
public
class CachedCountFieldSpec
	implements CachedAggregateFieldSpec {

	@DataParent
	CachedViewSpec cachedView;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute
	String when;

}
