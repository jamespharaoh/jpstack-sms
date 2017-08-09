package wbs.framework.entity.model;

import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.build.ModelBuilderManager;
import wbs.framework.entity.build.ModelFieldBuilderContext;
import wbs.framework.entity.build.ModelFieldBuilderTarget;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;

@Accessors (fluent = true)
@PrototypeComponent ("compositeModelBuilder")
public
class CompositeModelBuilder <DataType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelBuilderManager modelBuilderManager;

	@SingletonDependency
	SchemaNamesHelper schemaNamesHelper;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	// properties

	@Getter @Setter
	RecordSpec modelMeta;

	// state

	PluginRecordModelSpec pluginModel;
	PluginSpec plugin;

	CompositeModelImplementation <?> model;

	String modelClassName;
	String recordClassNameFull;
	Class <ModelType> modelClass;

	// implementation

	public
	Model <?> build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			plugin =
				modelMeta.plugin ();

			// model class

			modelClassName =
				stringFormat (
					"%s",
					capitalise (
						modelMeta.name ()));

			recordClassNameFull =
				stringFormat (
					"%s.model.%s",
					plugin.packageName (),
					modelClassName);

			modelClass =
				genericCastUnchecked (
					classForNameRequired (
						recordClassNameFull));

			// model

			model =
				new CompositeModelImplementation <ModelType> ()

				.objectClass (
					modelClass)

				.objectName (
					modelMeta.name ())

			;

			// model fields

			ModelFieldBuilderContext context =
				new ModelFieldBuilderContext ()

				.modelMeta (
					modelMeta)

				.modelClass (
					modelClass);

			ModelFieldBuilderTarget target =
				new ModelFieldBuilderTarget ()

				.model (
					model)

				.fields (
					model.fields ())

				.fieldsByName (
					model.fieldsByName ());

			modelBuilderManager.build (
				taskLogger,
				context,
				modelMeta.fields (),
				target);

			modelBuilderManager.build (
				taskLogger,
				context,
				modelMeta.collections (),
				target);

			// and return

			return model;

		}

	}

	public
	static Annotation findAnnotation (
			Annotation[] annotations,
			Class<? extends Annotation> metaAnnotation) {

		for (Annotation annotation
				: annotations) {

			if (annotation.annotationType ()
					.isAnnotationPresent (metaAnnotation))
				return annotation;

		}

		return null;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation.
	 */
	public
	Object annotationParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			Object defaultValue) {

		for (Method method
				: annotation.annotationType ().getMethods ()) {

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				return method.invoke (annotation);

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation. Specifically for string values, this will
	 * return a default value if the meta annotation is not present or if the
	 * value is the empty string.
	 */
	public
	String annotationStringParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			String defaultValue) {

		for (
			Method method
				: annotation.annotationType ().getMethods ()
		) {

			if (method.getReturnType () != String.class)
				continue;

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				String value =
					(String)
					method.invoke (annotation);

				if (value.isEmpty ())
					return defaultValue;

				return value;

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

}
