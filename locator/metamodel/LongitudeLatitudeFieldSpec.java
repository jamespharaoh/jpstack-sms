package wbs.sms.locator.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("longitude-latitude-field")
@PrototypeComponent ("longitudeLatitudeFieldSpec")
public
class LongitudeLatitudeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "columns")
	String columnNames;

	@DataAttribute
	Boolean nullable;

}
