package wbs.sms.messageset.console;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextPrivLookup;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.lookup.BooleanLookup;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleMetaModuleImplementation;
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
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.core.console.CoreAuthAction;

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("objectSmsMessageSetPageBuilder")
public
class ObjectSmsMessageSetPageBuilder <
	ObjectType extends Record <ObjectType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <CoreAuthAction> authActionProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextPrivLookup> contextPrivLookupProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	ComponentProvider <MessageSetAction> messageSetActionProvider;

	@PrototypeDependency
	ComponentProvider <MessageSetPart> messageSetPartProvider;

	@PrototypeDependency
	ComponentProvider <SimpleMessageSetFinder> simpleMessageSetFinderProvider;

	@PrototypeDependency
	ComponentProvider <TabContextResponder> tabContextResponderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectSmsMessageSetPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String privKey;
	String tabName;
	String tabLabel;
	String fileName;
	String responderName;

	BooleanLookup canViewLookup;
	BooleanLookup canUpdateLookup;
	MessageSetFinder messageSetFinder;

	// build meta

	public
	void buildMeta (
			@NonNull ConsoleMetaModuleImplementation consoleMetaModule) {

	}

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
					"build");

		) {

			setDefaults ();

			prepare (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildTab (
					taskLogger,
					resolvedExtensionPoint);

				buildFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			canViewLookup =
				contextPrivLookupProvider.provide (
					taskLogger)

				.addPrivKey (
					privKey)

			;

			canUpdateLookup =
				contextPrivLookupProvider.provide (
					taskLogger)

				.addPrivKey (
					privKey)

			;

			messageSetFinder =
				simpleMessageSetFinderProvider.provide (
					taskLogger)

				.objectLookup (
					consoleHelper)

				.code (
					spec.messageSetCode ())

			;

		}

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",
				contextTab.provide (
					taskLogger)

					.name (
						tabName)

					.defaultLabel (
						tabLabel)

					.localFile (
						fileName)

					.privKeys (
						singletonList (
							privKey)),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFile");

		) {

			PagePartFactory partFactory =
				parentTransaction -> {

				try (

					NestedTransaction transaction =
						parentTransaction.nestTransaction (
							logContext,
							"buildPagePart");

				) {

					return messageSetPartProvider.provide (
						transaction)

						.messageSetFinder (
							messageSetFinder)

					;

				}

			};

			ComponentProvider <WebResponder> responderProvider =
				taskLoggerNested ->
					tabContextResponderProvider.provide (
						taskLoggerNested)

				.tab (
					tabName)

				.title (
					tabLabel)

				.pagePartFactory (
					partFactory)

			;

			ComponentProvider <WebAction> getActionProvider =
				taskLoggerNested ->
					authActionProvider.provide (
						taskLoggerNested)

				.lookup (
					canViewLookup)

				.normalResponderProvider (
					responderProvider)

			;

			ComponentProvider <WebAction> postActionProvider =
				taskLoggerNested ->
					messageSetActionProvider.provide (
						taskLoggerNested)

				.responder (
					responderProvider)

				.messageSetFinder (
					messageSetFinder)

				.privLookup (
					canUpdateLookup)

			;

			consoleModule.addContextFile (
				fileName,
				consoleFileProvider.provide (
					taskLogger)

					.getResponderProvider (
						responderProvider)

					.getActionProvider (
						getActionProvider)

					.postActionProvider (
						postActionProvider)

					.privName (
						taskLogger,
						privKey),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			stringFormat (
				"%s.manage",
				consoleHelper.objectName ());

		tabName =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				spec.name ());

		tabLabel =
			capitalise (
				camelToSpaces (
					spec.name ()));

		fileName =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				spec.name ());

		responderName =
			stringFormat (
				"%s%sResponder",
				container.newBeanNamePrefix (),
				capitalise (
					spec.name ()));

	}

}
