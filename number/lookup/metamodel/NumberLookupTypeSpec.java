package wbs.sms.number.lookup.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("number-lookup-type")
@PrototypeComponent ("numberLookupTypeSpec")
public
class NumberLookupTypeSpec
	implements ModelDataSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String subject;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String description;

}
