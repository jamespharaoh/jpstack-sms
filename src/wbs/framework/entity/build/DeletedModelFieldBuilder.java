package wbs.framework.entity.build;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.camelToUnderscore;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.meta.fields.DeletedFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("deletedModelFieldBuilder")
@ModelBuilder
public
class DeletedModelFieldBuilder
	implements BuilderComponent {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	DeletedFieldSpec spec;

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
					"deleted");

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
					ModelFieldType.deleted)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					Boolean.class)

				.nullable (
					false)

				.columnNames (
					ImmutableList.of (
						ifNull (
							spec.columnName (),
							camelToUnderscore (
								fieldName))))

				.columnSqlTypes (
					ImmutableList.of (
						"boolean"))

			;

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

			if (target.model ().deletedField () != null)
				throw new RuntimeException ();

			target.model ().deletedField (
				modelField);

		}

	}

}
