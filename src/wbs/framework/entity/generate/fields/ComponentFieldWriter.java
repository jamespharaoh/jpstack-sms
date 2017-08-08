package wbs.framework.entity.generate.fields;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

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
import wbs.framework.component.scaffold.PluginCompositeModelSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.entity.generate.ModelRecordGenerator;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.ComponentFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("componentFieldWriter")
@ModelWriter
public
class ComponentFieldWriter
	implements BuilderComponent {

	// singleton dependency

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ComponentFieldSpec spec;

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

			PluginCompositeModelSpec targetModel =
				mapItemForKeyRequired (
					pluginManager.pluginCompositeModelsByName (),
					spec.typeName ());

			// write field

			new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeNameFormat (
					"%s.model.%s",
					targetModel.plugin ().packageName (),
					capitalise (
						spec.typeName ()))

				.propertyName (
					ifNull (
						spec.name (),
						spec.typeName ()))

				.defaultValue (
					imports ->
						stringFormat (
							"new %s ()",
							imports.registerFormat (
								"%s.model.%s",
								targetModel.plugin ().packageName (),
								capitalise (
									spec.typeName ()))))

				.setUpdatedFieldName (
					ModelRecordGenerator.recordUpdatedFieldName)

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ())

			;

		}

	}

}
