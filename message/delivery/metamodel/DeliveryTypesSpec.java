package wbs.sms.message.delivery.metamodel;

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
@DataClass ("delivery-types")
@PrototypeComponent ("deliveryTypesSpec")
@ModelMetaData
public
class DeliveryTypesSpec {

	@DataChildren (
		direct = true)
	List<DeliveryTypeSpec> deliveryTypes =
		new ArrayList<DeliveryTypeSpec> ();

}
