package wbs.sms.message.batch.model;

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
@DataClass ("batch-types")
@PrototypeComponent ("batchTypesSpec")
@ModelMetaData
public
class BatchTypesSpec {

	@DataChildren (
		direct = true)
	List<BatchTypeSpec> batchTypes =
		new ArrayList<BatchTypeSpec> ();

}
