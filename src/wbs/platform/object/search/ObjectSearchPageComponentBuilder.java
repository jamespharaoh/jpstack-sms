package wbs.platform.object.search;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.NonNull;

import wbs.console.component.ConsoleComponentBuilderComponent;
import wbs.console.component.ConsoleComponentBuilderContext;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.entity.record.IdObject;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("objectSearchPageComponentBuilder")
public
class ObjectSearchPageComponentBuilder <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable,
	ResultType extends IdObject
>
	implements ConsoleComponentBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ConsoleComponentBuilderContext context;

	@BuilderSource
	ObjectSearchPageSpec spec;

	@BuilderTarget
	ComponentRegistryBuilder target;

	// build

	@BuildMethod
	@Override
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

			String objectType =
				ifNull (
					spec.objectTypeName (),
					context.objectType ());

			String consoleHelperName =
				stringFormat (
					"%sConsoleHelper",
					hyphenToCamel (
						objectType));

			String searchPartFactoryName =
				ifNull (
					spec.searchResponderName (),
					stringFormat (
						"%s%sPartFactory",
						context.newComponentNamePrefix (),
						capitalise (
							spec.name ())));

			String searchResponderName =
				ifNull (
					spec.searchResponderName (),
					stringFormat (
						"%s%sResponder",
						context.newComponentNamePrefix (),
						capitalise (
							spec.name ())));

			String resultsPartFactoryName =
				ifNull (
					spec.searchResultsResponderName (),
					stringFormat (
						"%s%sResultsPartFactory",
						context.newComponentNamePrefix (),
						capitalise (
							spec.name ())));

			String resultsResponderName =
				ifNull (
					spec.searchResultsResponderName (),
					stringFormat (
						"%s%sResultsResponder",
						context.newComponentNamePrefix (),
						capitalise (
							spec.name ())));

			Class <?> searchClass =
				classForNameRequired (
					spec.searchClassName ());

			Class <?> resultClass =
				ifNotNullThenElse (
					spec.resultsClassName (),
					() -> classForNameRequired (
						spec.resultsClassName ()),
					() -> pluginManager.recordModelClass (
						objectType));

			String sessionKey =
				stringFormat (
					"%s.%s",
					context.pathPrefix (),
					spec.name ());

			String fileName =
				ifNull (
					spec.fileName (),
					stringFormat (
						"%s.%s",
						context.pathPrefix (),
						spec.name ()));

			String tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						context.pathPrefix (),
						spec.name ()));

			String searchTitle =
				capitalise (
					stringFormat (
						"%s search",
						context.friendlyName ()));

			String resultsTitle =
				capitalise (
					stringFormat (
						"%s search results",
						context.friendlyName ()));

			Map <String, String> resultsModes =
				new LinkedHashMap<> ();

			boolean haveResultsFormTypeName =
				isNotNull (
					spec.resultsFormTypeName ());

			boolean haveResultsModes =
				collectionIsNotEmpty (
					spec.resultsModes ());

			if (haveResultsFormTypeName && haveResultsModes) {
				throw new RuntimeException ();
			}

			if (haveResultsFormTypeName) {

				String resultsModeName =
					stringFormat (
						"%s%sResultsMode0",
						context.newComponentNamePrefix (),
						capitalise (
							spec.name ()));

				target.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						resultsModeName)

					.componentClass (
						ObjectSearchResultsMode.class)

					.scope (
						"singleton")

					.hide (
						true)

					.addValueProperty (
						"name",
						optionalOf (
							"normal"))

					.addReferencePropertyFormat (
						"formType",
						"singleton",
						"%s%sFormType",
						hyphenToCamel (
							context.consoleModule ().name ()),
						hyphenToCamelCapitalise (
							spec.resultsFormTypeName ()))

				);

				resultsModes.put (
					"normal",
					resultsModeName);

			} else {

				for (
					ObjectSearchResultsModeSpec resultsModeSpec
						: spec.resultsModes ()
				) {

					String resultsModeName =
						stringFormat (
							"%s%sResultsMode%s",
							context.newComponentNamePrefix (),
							capitalise (
								spec.name ()),
							integerToDecimalString (
								collectionSize (
									resultsModes)));

					target.registerDefinition (
						taskLogger,
						new ComponentDefinition ()

						.name (
							resultsModeName)

						.componentClass (
							ObjectSearchResultsMode.class)

						.scope (
							"singleton")

						.hide (
							true)

						.addValueProperty (
							"name",
							optionalOf (
								resultsModeSpec.name ()))

						.addReferencePropertyFormat (
							"formType",
							"singleton",
							"%s%sFormType",
							hyphenToCamel (
								context.consoleModule ().name ()),
							hyphenToCamelCapitalise (
								resultsModeSpec.formTypeName ()))

					);

					resultsModes.put (
						resultsModeSpec.name (),
						resultsModeName);

				}

			}

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					searchPartFactoryName)

				.scope (
					"singleton")

				.componentClass (
					ObjectSearchPartFactory.class)

				.hide (
					true)

				.addReferenceProperty (
					"consoleHelper",
					"singleton",
					consoleHelperName)

				.addValueProperty (
					"searchClass",
					optionalOf (
						searchClass))

				.addValueProperty (
					"sessionKey",
					optionalOf (
						sessionKey))

				.addReferencePropertyFormat (
					"searchFormType",
					"singleton",
					"%s%sFormType",
					hyphenToCamel (
						context.consoleModule ().name ()),
					hyphenToCamelCapitalise (
						spec.searchFormTypeName ()))

				.addValueProperty (
					"fileName",
					optionalOf (
						fileName))

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					resultsPartFactoryName)

				.scope (
					"singleton")

				.componentClass (
					ObjectSearchResultsPartFactory.class)

				.hide (
					true)

				.addReferenceProperty (
					"consoleHelper",
					"singleton",
					consoleHelperName)

				.addReferenceMapProperty (
					"resultsModes",
					"singleton",
					resultsModes)

				.addValueProperty (
					"resultsClass",
					optionalOf (
						resultClass))

				.addValueProperty (
					"resultsDaoMethodName",
					optionalFromNullable (
						spec.resultsDaoMethodName ()))

				.addValueProperty (
					"sessionKey",
					optionalOf (
						sessionKey))

				.addValueProperty (
					"itemsPerPage",
					optionalOf (
						100l))

				.addValueProperty (
					"targetContextTypeName",
					optionalOfFormat (
						"%s:combo",
						hyphenToCamel (
							objectType)))

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					searchResponderName)

				.scope (
					"prototype")

				.componentClass (
					TabContextResponder.class)

				.hide (
					true)

				.addValueProperty (
					"tab",
					optionalOf (
						tabName))

				.addValueProperty (
					"title",
					optionalOf (
						searchTitle))

				.addReferenceProperty (
					"pagePartFactory",
					"singleton",
					searchPartFactoryName)

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					resultsResponderName)

				.scope (
					"prototype")

				.componentClass (
					TabContextResponder.class)

				.hide (
					true)

				.addValueProperty (
					"tab",
					optionalOf (
						tabName))

				.addValueProperty (
					"title",
					optionalOf (
						resultsTitle))

				.addReferenceProperty (
					"pagePartFactory",
					"singleton",
					resultsPartFactoryName)

			);

		}

	}

}
