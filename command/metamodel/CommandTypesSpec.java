package wbs.sms.command.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("command-types")
@PrototypeComponent ("commandTypesSpec")
@ModelMetaData
public
class CommandTypesSpec {

	@DataChildren (
		direct = true)
	List<CommandTypeSpec> commandTypes =
		new ArrayList<CommandTypeSpec> ();

}
