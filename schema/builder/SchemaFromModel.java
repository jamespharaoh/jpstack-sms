package wbs.framework.schema.builder;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.model.Schema;
import wbs.framework.schema.model.SchemaTable;

@Accessors (fluent = true)
@PrototypeComponent ("schemaFromAnnotations")
public
class SchemaFromModel {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SchemaTableFromModel> schemaTableFromModelProvider;

	// properties

	@Getter @Setter
	Map <String, List <String>> enumTypes;

	@Getter @Setter
	Map <Class <?>, Model <?>> modelsByClass;

	public
	Schema build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			Schema schema =
				new Schema ()
					.enumTypes (enumTypes);

			for (
				Model <?> model
					: modelsByClass.values ()
			) {

				SchemaTable schemaTable =
					schemaTableFromModelProvider.provide (
						taskLogger)

					.modelsByClass (
						modelsByClass)

					.model (
						model)

					.build (
						taskLogger)

				;

				if (schemaTable == null)
					continue;

				schema.addTable (
					schemaTable);

			}

			return schema;

		}

	}

}
