package wbs.sms.locator.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("longitude-latitude-field")
@PrototypeComponent ("longitudeLatitudeFieldSpec")
@ModelMetaData
public
class LongitudeLatitudeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		value = "columns")
	String columnNames;

	@DataAttribute
	Boolean nullable;

}
