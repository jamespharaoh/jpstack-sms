package wbs.sms.message.core.console;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("messageConsolePluginManager")
public
class MessageConsolePluginManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	Map <String, MessageConsolePlugin> plugins;

	// state

	Map <String, MessageConsolePlugin> pluginsByCode;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			pluginsByCode =
				mapWithDerivedKey (
					plugins.values (),
					MessageConsolePlugin::getCode);

		}

	}

	// implementation

	public
	MessageConsolePlugin getPlugin (
			@NonNull String code) {

		return pluginsByCode.get (
			code);

	}

}
