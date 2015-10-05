package wbs.sms.message.delivery.model;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("delivery-type")
@PrototypeComponent ("deliveryTypeSpec")
@ModelMetaData
public
class DeliveryTypeSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String description;

}
