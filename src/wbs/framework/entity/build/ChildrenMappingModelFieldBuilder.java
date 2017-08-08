package wbs.framework.entity.build;

import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.lang3.reflect.TypeUtils;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.collections.ChildrenMappingSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("childrenMappingModelFieldBuilder")
@ModelBuilder
public
class ChildrenMappingModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

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

			String fieldName =
				ifNull (
					spec.name (),
					naivePluralise (
						spec.typeName ()));

			PluginRecordModelSpec fieldTypePluginModel =
				pluginManager.pluginRecordModelsByName ().get (
					spec.typeName ());

			PluginSpec fieldTypePlugin =
				fieldTypePluginModel.plugin ();

			String fullFieldTypeName =
				stringFormat (
					"%s.model.%sRec",
					fieldTypePlugin.packageName (),
					capitalise (
						spec.typeName ()));

			Class <?> fieldTypeClass =
				classForNameRequired (
					fullFieldTypeName);

			if (
				doesNotContain (
					mapTypeClasses.keySet (),
					spec.mapType ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Invalid map type %s ",
						spec.mapType (),
						"for model child field %s.%s",
						context.modelMeta.name (),
						fieldName));

			}

			Class <?> mapTypeClass =
				mapTypeClasses.get (
					spec.mapType ());

			// create model field

			ModelField modelField =
				new ModelField ()

				.model (
					target.model ())

				.parentField (
					context.parentModelField ())

				.name (
					fieldName)

				.label (
					camelToSpaces (
						fieldName))

				.type (
					ModelFieldType.collection)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					Map.class)

				.parameterizedType (
					TypeUtils.parameterize (
						Map.class,
						mapTypeClass,
						fieldTypeClass))

				.collectionKeyType (
					mapTypeClass)

				.collectionValueType (
					fieldTypeClass)

				.whereSql (
					spec.whereSql ())

				.orderSql (
					spec.orderSql ())

				.joinColumnName (
					spec.joinColumnName ())

				.mappingKeyColumnName (
					spec.mapColumnName ());

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

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
