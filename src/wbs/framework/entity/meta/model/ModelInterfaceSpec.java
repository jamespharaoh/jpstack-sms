package wbs.framework.entity.meta.model;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("interface")
@PrototypeComponent ("modelInterfaceSpec")
public
class ModelInterfaceSpec
	implements ModelDataSpec {

	@DataAncestor
	RecordSpec modelMeta;

	@DataAttribute (
		name = "package")
	String packageName;

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true,
		childElement = "parameter",
		valueAttribute = "value")
	List <String> parameters;

}
