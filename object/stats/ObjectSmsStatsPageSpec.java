package wbs.sms.object.stats;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-sms-stats-page")
@PrototypeComponent ("objectSmsStatsPageSpec")
@ConsoleModuleData
public
class ObjectSmsStatsPageSpec {

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String privKey;

}
