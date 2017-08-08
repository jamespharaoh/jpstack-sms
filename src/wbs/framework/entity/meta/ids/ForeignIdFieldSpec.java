package wbs.framework.entity.meta.ids;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("foreign-id-field")
@PrototypeComponent ("foreignIdFieldSpec")
public
class ForeignIdFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		name = "column")
	String columnName;

}
