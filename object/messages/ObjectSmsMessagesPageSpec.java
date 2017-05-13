package wbs.sms.object.messages;

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
@DataClass ("object-sms-messages-page")
@PrototypeComponent ("objectSmsMessagesPageSpec")
public
class ObjectSmsMessagesPageSpec
	implements ConsoleModuleData {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String privKey;

	@DataAttribute
	String tabName;

	@DataAttribute
	String fileName;

	@DataAttribute
	String responderName;

}
