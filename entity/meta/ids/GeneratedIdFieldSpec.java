package wbs.framework.entity.meta.ids;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

@Accessors (fluent = true)
@Data
@DataClass ("generated-id-field")
@PrototypeComponent ("generatedIdFieldSpec")
public
class GeneratedIdFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		name = "sequence")
	String sequenceName;

}
