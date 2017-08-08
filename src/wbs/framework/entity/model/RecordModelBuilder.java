package wbs.framework.entity.model;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToUnderscore;
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
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;

@Accessors (fluent = true)
@PrototypeComponent ("recordModelBuilder")
public
class RecordModelBuilder <RecordType extends Record <RecordType>> {

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

	RecordModelImplementation <?> model;

	String modelClassName;
	String recordClassNameFull;
	Class <RecordType> modelClass;

	String objectHelperClassName;
	String objectHelperClassNameFull;
	Class <ObjectHelper <RecordType>> objectHelperClass;

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

			// record class

			modelClassName =
				stringFormat (
					"%sRec",
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

			// object helper class

			objectHelperClassName =
				stringFormat (
					"%sObjectHelper",
					capitalise (
						modelMeta.name ()));

			objectHelperClassNameFull =
				stringFormat (
					"%s.model.%s",
					plugin.packageName (),
					objectHelperClassName);

			Class <ObjectHelper <RecordType>> objectHelperClassTemp =
				genericCastUnchecked (
					classForNameRequired (
						objectHelperClassNameFull));

			objectHelperClass =
				objectHelperClassTemp;

			// model

			model =
				new RecordModelImplementation <RecordType> ()

				.objectClass (
					modelClass)

				.objectName (
					modelMeta.name ())

				.oldObjectName (
					modelMeta.oldName ())

				.objectTypeCode (
					camelToUnderscore (
						ifNull (
							modelMeta.oldName (),
							modelMeta.name ())))

				.tableName (
					ifNull (
						modelMeta.tableName (),
						schemaNamesHelper.tableName (
							modelClass)))

				.create (
					ifNull (
						modelMeta.create (),
						true))

				.mutable (
					ifNull (
						modelMeta.mutable (),
						true))

				.helperClass (
					objectHelperClass)

				.cachedView (
					modelMeta.cachedView ())

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
