package wbs.framework.entity.build;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.camelToUnderscore;

import java.util.Date;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.meta.fields.TimestampFieldSpec;
import wbs.framework.entity.meta.fields.TimestampFieldSpec.ColumnType;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.hibernate.TimestampAsIsoStringUserType;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("timestampModelFieldBuilder")
@ModelBuilder
public
class TimestampModelFieldBuilder
	implements BuilderComponent {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	TimestampFieldSpec spec;

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
				spec.name ();

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
					ModelFieldType.simple)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					valueTypeByColumnType.get (
						spec.columnType ()))

				.nullable (
					ifNull (
						spec.nullable (),
						false))

				.columnNames (
					ImmutableList.<String>of (
						ifNull (
							spec.columnName (),
							camelToUnderscore (
								fieldName))))

				.columnSqlTypes (
					ImmutableList.of (
						sqlTypeByColumnType.get (
							spec.columnType ())))

				.hibernateTypeHelper (
					optionalOrNull (
						hibernateTypeHelperByColumnType.get (
							spec.columnType ())))

			;

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

			if (target.model ().timestampField () == null) {

				target.model ().timestampField (
					modelField);

			}

		}

	}

	public final static
	Map <ColumnType, Optional <Class <?>>> hibernateTypeHelperByColumnType =
		ImmutableMap.<ColumnType, Optional <Class <?>>> builder ()

		.put (
			ColumnType.postgresql,
			Optional.of (
				TimestampWithTimezoneUserType.class))

		.put (
			ColumnType.iso,
			Optional.of (
				TimestampAsIsoStringUserType.class))

		.build ();

	public final static
	Map <ColumnType, Optional <String>> sqlTypeByColumnType =
		ImmutableMap.<ColumnType, Optional <String>> builder ()

		.put (
			ColumnType.postgresql,
			"timestamp with time zone")

		.put (
			ColumnType.iso,
			"text")

		.put (
			ColumnType.unix,
			"bigint")

		.build ();

	public final static
	Map <ColumnType, Class <?>> valueTypeByColumnType =
		ImmutableMap.<ColumnType, Class <?>> builder ()

		.put (
			ColumnType.iso,
			Instant.class)

		.put (
			ColumnType.postgresql,
			Date.class)

		.build ();

}
