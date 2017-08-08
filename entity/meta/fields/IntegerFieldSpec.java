package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("integer-field")
@PrototypeComponent ("integerFieldSpec")
public
class IntegerFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "default")
	Long defaultValue;

	@DataAttribute (
		name = "minimum")
	Long minimumValue;

	@DataAttribute (
		name = "maximum")
	Long maximumValue;

	@DataAttribute (
		name = "column")
	String columnName;

}
