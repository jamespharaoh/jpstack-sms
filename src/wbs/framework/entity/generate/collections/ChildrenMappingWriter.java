package wbs.framework.entity.generate.collections;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.collections.ChildrenMappingSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("childrenMappingWriter")
@ModelWriter
public
class ChildrenMappingWriter
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

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

			String fieldName =
				ifNull (
					spec.name (),
					naivePluralise (
						spec.typeName ()));

			PluginRecordModelSpec fieldTypePluginModel =
				pluginManager.pluginRecordModelsByName ().get (
					spec.typeName ());

			if (
				isNull (
					fieldTypePluginModel)
			) {

				throw new RuntimeException (
					stringFormat (
						"Field '%s.%s' ",
						context.modelMeta ().name (),
						fieldName,
						"has type '%s' ",
						spec.typeName (),
						"which does not exist"));


			}

			PluginSpec fieldTypePlugin =
				fieldTypePluginModel.plugin ();

			String fullFieldTypeName =
				stringFormat (
					"%s.model.%sRec",
					fieldTypePlugin.packageName (),
					capitalise (
						spec.typeName ()));

			Class <?> keyType =
				mapTypeClasses.get (
					spec.mapType ());

			// write field

			new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeName (
					imports ->
						stringFormat (
							"%s <%s, %s>",
							imports.register (
								Map.class),
							imports.register (
								keyType),
							imports.register (
								fullFieldTypeName)))

				.propertyName (
					fieldName)

				.defaultValue (
					imports ->
						stringFormat (
							"new %s <%s, %s> ()",
							imports.register (
								LinkedHashMap.class),
							imports.register (
								keyType),
							imports.register (
								fullFieldTypeName)))

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ());

		}

	}

	public final static
	Map <String, Class <?>> mapTypeClasses =
		ImmutableMap.<String, Class <?>> builder ()

		.put (
			"string",
			String.class)

		.put (
			"integer",
			Long.class)

		.build ();

}
