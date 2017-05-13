package wbs.sms.gsm.console;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("gsm-field")
@PrototypeComponent ("gsmFormFieldSpec")
public
class GsmFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String name;

	@Getter @Setter
	boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Integer minimumLength;

	@DataAttribute
	Integer maximumLength;

}
