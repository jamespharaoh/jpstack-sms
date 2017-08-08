package wbs.sms.route.sender.metamodel;

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
@DataClass ("senders")
@PrototypeComponent ("sendersSpec")
public
class SendersSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <SenderSpec> senders=
		new ArrayList<> ();

}
