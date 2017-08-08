package wbs.framework.entity.build;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

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
import wbs.framework.entity.meta.collections.ChildrenListSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("childrenListModelFieldBuilder")
@ModelBuilder
public
class ChildrenListModelFieldBuilder
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
	ChildrenListSpec spec;

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

			Class<?> fieldTypeClass =
				classForNameRequired (
					fullFieldTypeName);

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
					List.class)

				.parameterizedType (
					TypeUtils.parameterize (
						List.class,
						fieldTypeClass))

				.collectionKeyType (
					Integer.class)

				.collectionValueType (
					fieldTypeClass)

				.joinColumnName (
					spec.joinColumnName ())

				.whereSql (
					spec.whereSql ())

				.orderSql (
					spec.orderSql ())

				.listIndexColumnName (
					ifNull (
						spec.listIndexColumnName (),
						"index"));

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

		}

	}

}
