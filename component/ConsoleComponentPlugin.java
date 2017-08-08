package wbs.console.component;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableFilterByClass;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemByClass;
import static wbs.utils.collection.MapUtils.iterableTransformToMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.anyIsNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormSpec;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.forms.core.ConsoleFormTypeFactory;
import wbs.console.forms.core.ConsoleFormsSpec;
import wbs.console.forms.core.ConsoleMultiFormType;
import wbs.console.forms.core.ConsoleMultiFormTypeFactory;
import wbs.console.forms.core.DynamicFieldsProvider;
import wbs.console.forms.core.StaticFieldsProvider;
import wbs.console.forms.core.StaticFieldsProviderFactory;
import wbs.console.forms.core.StaticObjectFieldsProviderFactory;
import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.helper.enums.EnumConsoleHelperFactory;
import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.helper.provider.ConsoleHelperProviderFactory;
import wbs.console.helper.provider.ConsoleHelperProviderSpec;
import wbs.console.module.ConsoleMetaModule;
import wbs.console.module.ConsoleMetaModuleFactory;
import wbs.console.module.ConsoleModule;
import wbs.console.module.ConsoleModuleFactory;
import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleModuleSpecManager;

import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginConsoleModuleSpec;
import wbs.framework.component.scaffold.PluginCustomTypeSpec;
import wbs.framework.component.scaffold.PluginEnumTypeSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleComponentPlugin")
public
class ConsoleComponentPlugin
	implements ComponentPlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleComponentBuilder consoleComponentBuilder;

	@SingletonDependency
	ConsoleModuleSpecManager consoleModuleSpecManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			plugin.models ().models ().forEach (
				pluginModelSpec ->
					registerConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginModelSpec));

			plugin.models ().enumTypes ().forEach (
				pluginEnumTypeSpec ->
					registerEnumConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginEnumTypeSpec));

			plugin.models ().customTypes ().forEach (
				pluginCustomTypeSpec ->
					registerCustomConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginCustomTypeSpec));

			plugin.consoleModules ().forEach (
				consoleModuleSpec ->
					registerConsoleModule (
						taskLogger,
						componentRegistry,
						consoleModuleSpec));

		}

	}

	// private implementation

	private
	void registerConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginRecordModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleHelper");

		) {

			String consoleHelperComponentName =
				stringFormat (
					"%sConsoleHelper",
					model.name ());

			// console helper

			String consoleHelperClassName =
				stringFormat (
					"%s.console.%sConsoleHelper",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Optional <Class <?>> consoleHelperClassOptional =
				classForName (
					consoleHelperClassName);

			if (
				optionalIsNotPresent (
					consoleHelperClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					consoleHelperClassName);

				return;

			}

			// console helper implemenation

			String consoleHelperImplementationClassName =
				stringFormat (
					"%s.console.%sConsoleHelperImplementation",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Optional <Class <?>> consoleHelperImplementationClassOptional =
				classForName (
					consoleHelperImplementationClassName);

			if (
				optionalIsNotPresent (
					consoleHelperImplementationClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					consoleHelperImplementationClassName);

				return;

			}

			Class <?> consoleHelperImplementationClass =
				consoleHelperImplementationClassOptional.get ();

			// component definition

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					consoleHelperComponentName)

				.componentClass (
					consoleHelperImplementationClass)

				.scope (
					"singleton")

			);

		}

	}

	private
	void registerEnumConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginEnumTypeSpec enumType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerEnumConsoleHelper");

		) {

			String enumClassName =
				stringFormat (
					"%s.model.%s",
					enumType.plugin ().packageName (),
					capitalise (
						enumType.name ()));

			Class <?> enumClass;

			try {

				enumClass =
					Class.forName (
						enumClassName);

			} catch (ClassNotFoundException exception) {

				taskLogger.errorFormat (
					"No such class %s",
					enumClassName);

				return;

			}

			String enumConsoleHelperComponentName =
				stringFormat (
					"%sConsoleHelper",
					enumType.name ());

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					enumConsoleHelperComponentName)

				.componentClass (
					EnumConsoleHelper.class)

				.factoryClass (
					genericCastUnchecked (
						EnumConsoleHelperFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"enumClass",
					optionalOf (
						enumClass))

			);

		}

	}

	private
	void registerCustomConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginCustomTypeSpec customType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerCustomConsoleHelper");

		) {

			String customClassName =
				stringFormat (
					"%s.model.%s",
					customType.plugin ().packageName (),
					capitalise (
						customType.name ()));

			Optional <Class <?>> customClassOptional =
				classForName (
					customClassName);

			if (
				optionalIsNotPresent (
					customClassOptional)

			) {

				taskLogger.errorFormat (
					"No such class %s",
					customClassName);

				return;

			}

			if (
				isNotSubclassOf (
					Enum.class,
					customClassOptional.get ())
			) {
				return;
			}

			Class <?> enumClass =
				customClassOptional.get ();

			String enumConsoleHelperComponentName =
				stringFormat (
					"%sConsoleHelper",
					customType.name ());

			if (
				componentRegistry.hasName (
					enumConsoleHelperComponentName)
			) {
				return;
			}

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					enumConsoleHelperComponentName)

				.componentClass (
					EnumConsoleHelper.class)

				.factoryClass (
					genericCastUnchecked (
						EnumConsoleHelperFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"enumClass",
					optionalOf (
						enumClass))

			);

		}

	}

	private
	void registerConsoleModule (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginConsoleModuleSpec pluginConsoleModuleSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleModule");

		) {

			// get module spec

			ConsoleModuleSpec consoleModuleSpec =
				mapItemForKeyRequired (
					consoleModuleSpecManager.specsByName (),
					pluginConsoleModuleSpec.name ());

			// register console meta module

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%sConsoleMetaModule",
						hyphenToCamel (
							consoleModuleSpec.name ())))

				.componentClass (
					ConsoleMetaModule.class)

				.factoryClass (
					genericCastUnchecked (
						ConsoleMetaModuleFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"consoleModuleSpec",
					optionalOf (
						consoleModuleSpec))

			);

			// register console module

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%sConsoleModule",
						hyphenToCamel (
							consoleModuleSpec.name ())))

				.componentClass (
					ConsoleModule.class)

				.factoryClass (
					genericCastUnchecked (
						ConsoleModuleFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"consoleModuleSpec",
					optionalOf (
						consoleModuleSpec))

			);

			// iterate over console helper providers

			for (
				ConsoleHelperProviderSpec consoleHelperProviderSpec
					: iterableFilterByClass (
						consoleModuleSpec.builders (),
						ConsoleHelperProviderSpec.class)
			) {

				registerConsoleHelperProvider (
					taskLogger,
					componentRegistry,
					consoleHelperProviderSpec);

			}

			// iterate over forms

			Optional <ConsoleFormsSpec> consoleFormsSpecOptional =
				iterableOnlyItemByClass (
					consoleModuleSpec.builders (),
					ConsoleFormsSpec.class);

			if (
				optionalIsPresent (
					consoleFormsSpecOptional)
			) {

				ConsoleFormsSpec consoleFormsSpec =
					optionalGetRequired (
						consoleFormsSpecOptional);

				consoleFormsSpec.forms ().forEach (
					consoleFormSpec ->
						registerConsoleFormType (
							taskLogger,
							componentRegistry,
							consoleModuleSpec,
							consoleFormSpec));

			}

			// descend console component builders

			consoleComponentBuilder.descend (
				taskLogger,
				consoleModuleSpec,
				consoleModuleSpec.builders (),
				componentRegistry,
				MissingBuilderBehaviour.error);

		}

	}

	private
	void registerConsoleHelperProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull ConsoleHelperProviderSpec spec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleHelperProvider");
		) {

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%sConsoleHelperProvider",
						spec.objectName ()))

				.scope (
					"singleton")

				.componentClass (
					ConsoleHelperProvider.class)

				.factoryClass (
					genericCastUnchecked (
						ConsoleHelperProviderFactory.class))

				.addValueProperty (
					"spec",
					optionalOf (
						spec))

			);

		}

	}

	private
	void registerConsoleFormType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull ConsoleModuleSpec moduleSpec,
			@NonNull ConsoleFormSpec formSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleFormType");

		) {

			boolean gotFields =
				anyIsNotNull (
					formSpec.columnFields (),
					formSpec.rowFields ());

			boolean gotSections =
				collectionIsNotEmpty (
					formSpec.sections ());

			boolean gotProvider =
				isNotNull (
					formSpec.fieldsProviderName ());

			if ((gotFields && gotProvider) && gotSections) {
				throw todo ();
			}

			Class <?> formClass =
				ifNotNullThenElse (
					formSpec.objectTypeName (),
					() -> pluginManager.recordModelClass (
						formSpec.objectTypeName ()),
					() -> classForNameRequired (
						formSpec.className ()));

			if (gotSections) {

				registerConsoleMultiFormType (
					taskLogger,
					componentRegistry,
					moduleSpec,
					formSpec);

				return;

			}

			// static fields provider

			String staticFieldsProviderName =
				stringFormat (
					"%s%sStaticFormFieldsProvider",
					hyphenToCamel (
						moduleSpec.name ()),
					hyphenToCamelCapitalise (
						formSpec.name ()));

			if (
				isNotNull (
					formSpec.objectTypeName ())
			) {

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						staticFieldsProviderName)

					.componentClass (
						StaticFieldsProvider.class)

					.factoryClass (
						genericCastUnchecked (
							StaticObjectFieldsProviderFactory.class))

					.scope (
						"singleton")

					.hide (
						true)

					.addValuePropertyFormat (
						"name",
						"%s.%s",
						moduleSpec.name (),
						formSpec.name ())

					.addReferencePropertyFormat (
						"consoleHelper",
						"singleton",
						"%sConsoleHelper",
						hyphenToCamel (
							formSpec.objectTypeName ()))

					.addValueProperty (
						"columnFieldSpecs",
						optionalOf (
							ifNull (
								formSpec.columnFields (),
								emptyList ())))

					.addValueProperty (
						"rowFieldSpecs",
						optionalOf (
							ifNull (
								formSpec.rowFields (),
								emptyList ())))

				);

			} else {

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						staticFieldsProviderName)

					.componentClass (
						StaticFieldsProvider.class)

					.factoryClass (
						genericCastUnchecked (
							StaticFieldsProviderFactory.class))

					.scope (
						"singleton")

					.hide (
						true)

					.addValueProperty (
						"name",
						optionalOfFormat (
							"%s.%s",
							moduleSpec.name (),
							formSpec.name ()))

					.addValueProperty (
						"containerClass",
						optionalOf (
							formClass))

					.addValueProperty (
						"columnFieldSpecs",
						optionalFromNullable (
							formSpec.columnFields ()))

					.addValueProperty (
						"rowFieldSpecs",
						optionalFromNullable (
							formSpec.rowFields ()))

				);

			}

			// dynamic fields provider

			String fieldsProviderName;

			if (gotProvider) {

				fieldsProviderName =
					stringFormat (
						"%s%sDynamicFormFieldsProvider",
						hyphenToCamel (
							moduleSpec.name ()),
						hyphenToCamelCapitalise (
							formSpec.name ()));

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						fieldsProviderName)

					.scope (
						"singleton")

					.componentClass (
						DynamicFieldsProvider.class)

					.addValueProperty (
						"name",
						optionalOfFormat (
							"%s.%s",
							moduleSpec.name (),
							formSpec.name ()))

					.addValueProperty (
						"containerClass",
						optionalOf (
							formClass))

					.addReferenceProperty (
						"staticFieldsProvider",
						"singleton",
						staticFieldsProviderName)

					.addReferenceProperty (
						"dynamicFieldsProvider",
						"singleton",
						formSpec.fieldsProviderName ())

				);

			} else {

				fieldsProviderName =
					staticFieldsProviderName;

			}

			// form type

			ComponentDefinition componentDefinition =
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%s%sFormType",
						hyphenToCamel (
							moduleSpec.name ()),
						hyphenToCamelCapitalise (
							formSpec.name ())))

				.componentClass (
					ConsoleFormType.class)

				.factoryClass (
					genericCastUnchecked (
						ConsoleFormTypeFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"containerClass",
					optionalOf (
						formClass))

				.addValueProperty (
					"formName",
					optionalOf (
						formSpec.name ()))

				.addValueProperty (
					"formType",
					optionalOf (
						formSpec.formType ()))

				.addReferenceProperty (
					"fieldsProvider",
					"singleton",
					fieldsProviderName)

			;

			if (
				isNotNull (
					formSpec.objectTypeName ())
			) {

				componentDefinition.addReferencePropertyFormat (
					"consoleHelper",
					"singleton",
					"%sConsoleHelper",
					hyphenToCamel (
						formSpec.objectTypeName ()));

			} else {

				componentDefinition.addValueProperty (
					"containerClass",
					optionalOf (
						classForNameRequired (
							formSpec.className ())));

			}

			componentRegistry.registerDefinition (
				taskLogger,
				componentDefinition);

		}

	}

	private
	void registerConsoleMultiFormType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull ConsoleModuleSpec moduleSpec,
			@NonNull ConsoleFormSpec formSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleMultiFormType");

		) {

			ComponentDefinition componentDefinition =
				new ComponentDefinition ()

				.name (
					stringFormat (
						"%s%sFormType",
						hyphenToCamel (
							moduleSpec.name ()),
						hyphenToCamelCapitalise (
							formSpec.name ())))

				.componentClass (
					ConsoleMultiFormType.class)

				.factoryClass (
					genericCastUnchecked (
						ConsoleMultiFormTypeFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"consoleModuleName",
					optionalOf (
						moduleSpec.name ()))

				.addValueProperty (
					"formName",
					optionalOf (
						formSpec.name ()))

				.addValueProperty (
					"formType",
					optionalOf (
						formSpec.formType ()))

				.addValueProperty (
					"sectionFields",
					optionalOf (
						iterableTransformToMap (
							formSpec.sections (),
							section ->
								section.name (),
							section ->
								section.fields ())))

			;

			if (
				isNotNull (
					formSpec.objectTypeName ())
			) {

				componentDefinition.addReferencePropertyFormat (
					"consoleHelper",
					"singleton",
					"%sConsoleHelper",
					hyphenToCamel (
						formSpec.objectTypeName ()));

			} else {

				componentDefinition.addValueProperty (
					"containerClass",
					optionalOf (
						classForNameRequired (
							formSpec.className ())));

			}

			componentRegistry.registerDefinition (
				taskLogger,
				componentDefinition);

		}

	}

}
