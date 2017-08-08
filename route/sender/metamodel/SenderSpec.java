package wbs.sms.route.sender.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

@Accessors (fluent = true)
@Data
@DataClass ("sender")
@PrototypeComponent ("senderSpec")
public
class SenderSpec
	implements ModelDataSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String description;

}
