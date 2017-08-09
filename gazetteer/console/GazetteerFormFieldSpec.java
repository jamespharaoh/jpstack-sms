package wbs.sms.gazetteer.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("gazetteer-field")
@PrototypeComponent ("gazetteerFormFieldSpec")
public
class GazetteerFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		name = "code-field")
	String codeFieldName;

	@DataAttribute (
		name = "location-field")
	String locationFieldName;

	@DataAttribute (
		name = "gazetteer")
	String gazetteerFieldName;

}
