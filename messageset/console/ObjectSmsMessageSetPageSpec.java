package wbs.sms.messageset.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-sms-message-set-page")
@PrototypeComponent ("objectSmsMessageSetPageSpec")
public
class ObjectSmsMessageSetPageSpec
	implements ConsoleModuleData {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "message-set")
	String messageSetCode;

}
