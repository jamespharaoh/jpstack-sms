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
import wbs.framework.entity.meta.fields.DescriptionFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("descriptionModelFieldBuilder")
@ModelBuilder
public
class DescriptionModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	DescriptionFieldSpec spec;

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
					"description");

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
					ModelFieldType.description)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					String.class)

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
						"text"))

			;

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

			if (target.model ().descriptionField () != null)
				throw new RuntimeException ();

			target.model ().descriptionField (
				modelField);

		}

	}

}
