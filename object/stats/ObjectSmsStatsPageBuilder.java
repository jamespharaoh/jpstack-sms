package wbs.sms.object.stats;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("objectSmsStatsPageBuilder")
public
class ObjectSmsStatsPageBuilder <
	ObjectType extends Record <ObjectType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	ComponentProvider <TabContextResponder> tabContextResponderProvider;

	@PrototypeDependency
	ComponentProvider <ObjectStatsPartFactory> objectStatsPartFactoryProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectSmsStatsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;
	String privKey;

	ComponentProvider <WebResponder> responderProvider;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildConsoleModule");

		) {

			setDefaults ();

			buildResponder (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						taskLogger,
						container.extensionPointName ())
			) {

				buildContextTab (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",
				contextTabProvider.provide (
					taskLogger)

					.name (
						consoleHelper.objectTypeCamel () + ".stats")

					.defaultLabel (
						"Stats")

					.localFile (
						consoleHelper.objectTypeCamel () + ".stats")

					.privKeys (
						privKey),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildContextFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextFile");

		) {

			consoleModule.addContextFile (
				consoleHelper.objectTypeCamel () + ".stats",
				consoleFileProvider.provide (
					taskLogger)

					.getResponderProvider (
						responderProvider)

					.privKeys (
						taskLogger,
						singletonList (
							privKey)),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildResponder");

		) {

			PagePartFactory partFactory =
				objectStatsPartFactoryProvider.provide (
					taskLogger)

				.localName (
					"/" + consoleHelper.objectTypeCamel () + ".stats")

				.objectLookup (
					consoleHelper);

			responderProvider =
				taskLoggerNested ->
					tabContextResponderProvider.provide (
						taskLoggerNested)

				.tab (
					consoleHelper.objectTypeCamel () + ".stats")

				.title (
					capitalise (
						consoleHelper.friendlyNameSingular () + " stats"))

				.pagePartFactory (
					partFactory)

			;

		}

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			ifNull (
				spec.privKey (),
				stringFormat (
					"%s.stats",
					consoleHelper.objectTypeCamel ()));

	}

}
