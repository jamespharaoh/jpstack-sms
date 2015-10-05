package wbs.sms.route.sender.model;

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
@DataClass ("senders")
@PrototypeComponent ("sendersSpec")
@ModelMetaData
public
class SendersSpec {

	@DataChildren (
		direct = true)
	List<SenderSpec> senders=
		new ArrayList<SenderSpec> ();

}
