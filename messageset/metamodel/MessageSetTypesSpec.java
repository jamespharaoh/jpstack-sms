package wbs.sms.messageset.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

@Accessors (fluent = true)
@Data
@DataClass ("message-set-types")
@PrototypeComponent ("messageSetTypesSpec")
public
class MessageSetTypesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <MessageSetTypeSpec> messageSetTypes =
		new ArrayList<> ();

}
