package wbs.sms.message.delivery.metamodel;

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
@DataClass ("delivery-types")
@PrototypeComponent ("deliveryTypesSpec")
public
class DeliveryTypesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <DeliveryTypeSpec> deliveryTypes =
		new ArrayList<> ();

}
