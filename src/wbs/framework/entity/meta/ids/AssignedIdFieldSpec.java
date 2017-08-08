package wbs.framework.entity.meta.ids;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("assigned-id-field")
@PrototypeComponent ("assignedIdFieldSpec")
public
class AssignedIdFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		name = "column")
	String columnName;

}
