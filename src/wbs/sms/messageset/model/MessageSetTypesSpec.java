package wbs.sms.messageset.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("message-set-types")
@PrototypeComponent ("messageSetTypesSpec")
@ModelMetaData
public
class MessageSetTypesSpec {

	@DataChildren (
		direct = true)
	List<MessageSetTypeSpec> messageSetTypes =
		new ArrayList<MessageSetTypeSpec> ();

}