package wbs.sms.message.core.console;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("messageConsolePluginManager")
public
class MessageConsolePluginManager {

	@Inject
	Map<String,MessageConsolePlugin> plugins =
		Collections.emptyMap ();

	Map<String,MessageConsolePlugin> pluginsByCode =
		new HashMap<String,MessageConsolePlugin> ();

	@PostConstruct
	public
	void init () {

		// add their plugins to our map

		for (Map.Entry<String,MessageConsolePlugin> pluginEntry
				: plugins.entrySet ()) {

			//String beanName = ent.getKey ();

			MessageConsolePlugin messageConsolePlugin =
				pluginEntry.getValue ();

			pluginsByCode.put (
				messageConsolePlugin.getCode (),
				messageConsolePlugin);

		}

	}

	public
	MessageConsolePlugin getPlugin (
			String code) {

		return pluginsByCode.get (
			code);

	}

}
