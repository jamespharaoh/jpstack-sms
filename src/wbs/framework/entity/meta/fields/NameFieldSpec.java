package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("name-field")
@PrototypeComponent ("nameFieldSpec")
public
class NameFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String columnName;

}
