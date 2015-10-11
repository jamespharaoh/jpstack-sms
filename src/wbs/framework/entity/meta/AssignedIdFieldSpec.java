package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("assigned-id-field")
@PrototypeComponent ("assignedIdFieldSpec")
@ModelMetaData
public
class AssignedIdFieldSpec
	implements ModelFieldSpec {

}