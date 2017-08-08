package wbs.framework.entity.meta.cachedview;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.entity.meta.model.ModelDataSpec;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = { "cachedView", "name" })
@ToString (of = { "name" })
@DataClass ("group-field")
@PrototypeComponent ("cachedGroupFieldSpec")
public
class CachedGroupFieldSpec
	implements ModelDataSpec {

	@DataParent
	CachedViewSpec cachedView;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		required = true)
	String source;

}
