package wbs.sms.object.stats;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;
import wbs.platform.console.spec.ConsoleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("object-sms-stats-page")
@PrototypeComponent ("objectSmsStatsPageSpec")
@ConsoleModuleData
public
class ObjectSmsStatsPageSpec {

	@DataAncestor
	ConsoleSpec consoleSpec;

	@DataAttribute
	String privKey;

}
