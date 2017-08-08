package wbs.framework.entity.meta.identities;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("identity-integer-field")
@PrototypeComponent ("identityIntegerFieldSpec")
public
class IdentityIntegerFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "column")
	String columnName;

}
