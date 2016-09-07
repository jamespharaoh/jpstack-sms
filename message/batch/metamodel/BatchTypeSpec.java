package wbs.sms.message.batch.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("batch-type")
@PrototypeComponent ("batchTypeSpec")
@ModelMetaData
public
class BatchTypeSpec {

	@DataAttribute
	String subject;

	@DataAttribute
	String batch;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String description;

}
