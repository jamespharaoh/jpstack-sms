package wbs.sms.object.stats;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.module.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.TabContextResponder;

@PrototypeComponent ("objectSmsStatsPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSmsStatsPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFileProvider;

	@Inject
	Provider<ConsoleContextTab> contextTabProvider;

	@Inject
	Provider<TabContextResponder> tabContextResponderProvider;

	@Inject
	Provider<ObjectStatsPartFactory> objectStatsPartFactoryProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectSmsStatsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;
	String privKey;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		buildResponder ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			"end",
			contextTabProvider.get ()

				.name (
					consoleHelper.objectName () + ".stats")

				.defaultLabel (
					"Stats")

				.localFile (
					consoleHelper.objectName () + ".stats")

				.privKeys (
					privKey),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			consoleHelper.objectName () + ".stats",

			consoleFileProvider.get ()

				.getResponderName (
					stringFormat (
						"%sStatsResponder",
						consoleHelper.objectName ()))

				.privKeys (
					Collections.singletonList (privKey)),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			objectStatsPartFactoryProvider.get ()

			.localName (
				"/" + consoleHelper.objectName () + ".stats")

			.objectLookup (
				consoleHelper);

		consoleModule.addResponder (
			consoleHelper.objectName () + "StatsResponder",

			tabContextResponderProvider.get ()

				.tab (
					consoleHelper.objectName () + ".stats")

				.title (
					capitalise (
						consoleHelper.friendlyName () + " stats"))

				.pagePartFactory (
					partFactory));

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			ifNull (
				spec.privKey (),
				stringFormat (
					"%s.stats",
					consoleHelper.objectName ()));

	}

}
