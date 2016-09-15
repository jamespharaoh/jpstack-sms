package wbs.sms.message.core.console;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("messageConsolePluginManager")
public
class MessageConsolePluginManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, MessageConsolePlugin> plugins;

	// state

	Map <String, MessageConsolePlugin> pluginsByCode;

	// life cycle

	@NormalLifecycleSetup
	public
	void init () {

		pluginsByCode =
			mapWithDerivedKey (
				plugins.values (),
				MessageConsolePlugin::getCode);

	}

	// implementation

	public
	MessageConsolePlugin getPlugin (
			@NonNull String code) {

		return pluginsByCode.get (
			code);

	}

}
