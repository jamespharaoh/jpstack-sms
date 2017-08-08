package wbs.framework.entity.generate.fields;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
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
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelRecordGenerator;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.ReferenceFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("referenceFieldWriter")
@ModelWriter
public
class ReferenceFieldWriter
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ReferenceFieldSpec spec;

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

			PluginRecordModelSpec fieldTypePluginModel =
				pluginManager.pluginRecordModelsByName ().get (
					spec.typeName ());

			if (
				isNull (
					fieldTypePluginModel)
			) {

				throw new RuntimeException (
					stringFormat (
						"Field type %s ",
						spec.typeName (),
						"does not exist while building reference field %s.%s",
						context.modelMeta ().name (),
						ifNull (
							spec.name (),
							spec.typeName ())));

			}

			PluginSpec fieldTypePlugin =
				fieldTypePluginModel.plugin ();

			String fullFieldTypeName =
				stringFormat (
					"%s.model.%sRec",
					fieldTypePlugin.packageName (),
					capitalise (
						spec.typeName ()));

			// write field

			new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeName (
					fullFieldTypeName)

				.propertyName (
					ifNull (
						spec.name (),
						spec.typeName ()))

				.setUpdatedFieldName (
					ModelRecordGenerator.recordUpdatedFieldName)

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ());

		}

	}

}
