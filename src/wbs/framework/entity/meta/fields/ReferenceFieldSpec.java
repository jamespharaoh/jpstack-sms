package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("reference-field")
@PrototypeComponent ("referenceFieldSpec")
public
class ReferenceFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "column")
	String columnName;

}
