package wbs.framework.entity.build;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Set;

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
import wbs.framework.entity.meta.collections.AssociativeCollectionSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("associativeCollectionModelFieldBuilder")
@ModelBuilder
public
class AssociativeCollectionModelFieldBuilder
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
	AssociativeCollectionSpec spec;

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

			Class<?> fieldTypeClass;

			if (
				stringEqualSafe (
					spec.typeName (),
					"string")
			) {

				fieldTypeClass =
					String.class;

			} else {

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

				fieldTypeClass =
					classForNameRequired (
						fullFieldTypeName);

			}

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
					ModelFieldType.associative)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					Set.class)

				.parameterizedType (
					TypeUtils.parameterize (
						Set.class,
						fieldTypeClass))

				.collectionKeyType (
					fieldTypeClass)

				.collectionValueType (
					fieldTypeClass)

				.associationTableName (
					spec.tableName ())

				.joinColumnName (
					spec.joinColumnName ())

				.foreignColumnName (
					spec.foreignColumnName ())

				.valueColumnName (
					spec.valueColumnName ())

				.whereSql (
					spec.whereSql ())

				.orderSql (
					spec.orderSql ())

				.owned (
					ifNull (
						spec.owned (),
						false));

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

		}

	}

}
