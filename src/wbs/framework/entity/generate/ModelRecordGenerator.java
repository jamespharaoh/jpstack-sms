package wbs.framework.entity.generate;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.io.FileUtils.directoryCreateWithParents;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaClassWriter;
import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.fields.DateFieldSpec;
import wbs.framework.entity.meta.fields.IntegerFieldSpec;
import wbs.framework.entity.meta.fields.ReferenceFieldSpec;
import wbs.framework.entity.meta.fields.TimestampFieldSpec;
import wbs.framework.entity.meta.identities.CodeFieldSpec;
import wbs.framework.entity.meta.identities.IdentityIntegerFieldSpec;
import wbs.framework.entity.meta.identities.IdentityReferenceFieldSpec;
import wbs.framework.entity.meta.identities.IdentityStringFieldSpec;
import wbs.framework.entity.meta.identities.IndexFieldSpec;
import wbs.framework.entity.meta.identities.MasterFieldSpec;
import wbs.framework.entity.meta.identities.ParentFieldSpec;
import wbs.framework.entity.meta.identities.ParentIdFieldSpec;
import wbs.framework.entity.meta.identities.ParentTypeFieldSpec;
import wbs.framework.entity.meta.identities.TypeCodeFieldSpec;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelInterfaceSpec;
import wbs.framework.entity.meta.model.ModelMetaType;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.record.CommonRecord;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.EventRecord;
import wbs.framework.entity.record.MajorRecord;
import wbs.framework.entity.record.MinorRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.RecordComponent;
import wbs.framework.entity.record.RootRecord;
import wbs.framework.entity.record.TypeRecord;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.AtomicFileWriter;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("modelRecordGenerator")
public
class ModelRecordGenerator {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelWriterManager modelWriterBuilder;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JavaClassWriter> javaClassWriterProvider;

	@PrototypeDependency
	ComponentProvider <JavaClassUnitWriter> javaClassUnitWriterProvider;

	@PrototypeDependency
	ComponentProvider <JavaPropertyWriter> javaPropertyWriterProvider;

	// properties

	@Getter @Setter
	PluginSpec plugin;

	@Getter @Setter
	RecordSpec modelMeta;

	// state

	String recordClassName;

	// implementation

	public
	void generateRecord (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"generateRecord");

		) {

			if (modelMeta.type ().record ()) {

				recordClassName =
					stringFormat (
						"%sRec",
						capitalise (
							modelMeta.name ()));

			} else if (modelMeta.type ().composite ()) {

				recordClassName =
					capitalise (
						modelMeta.name ());

			} else {

				throw new RuntimeException ();

			}

			// create directory

			String directory =
				stringFormat (
					"work/generated/%s/model",
					plugin.packageName ().replace ('.', '/'));

			directoryCreateWithParents (
				directory);

			// write interface

			String filename =
				stringFormat (
					"%s/%s.java",
					directory,
					recordClassName);

			try (

				AtomicFileWriter formatWriter =
					new AtomicFileWriter (
						filename);

			) {

				JavaClassUnitWriter classUnitWriter =
					javaClassUnitWriterProvider.provide (
						taskLogger)

					.formatWriter (
						formatWriter)

					.packageNameFormat (
						"%s.model",
						plugin.packageName ());

				JavaClassWriter modelWriter =
					javaClassWriterProvider.provide (
						taskLogger)

					.className (
						recordClassName)

					.addClassModifier (
						"public")

					.addImplements (
						imports ->
							stringFormat (
								"%s <%s>",
								imports.register (
									modelInterfacesByType.get (
										modelMeta.type ())),
								imports.registerFormat (
									"%s.model.%s",
									modelMeta.plugin ().packageName (),
									recordClassName)));

				for (
					ModelInterfaceSpec implementsInterface
						: modelMeta.recordInterfaces ()
				) {

					modelWriter.addImplementsName (
						stringFormat (
							"%s.%s",
							implementsInterface.packageName (),
							implementsInterface.name ()),
						implementsInterface.parameters ());

				}

				modelWriter

					.addBlock (
						this::writeConstructor)

					.addBlock (
						this::writeInternalFields)

					.addBlock (
						this::writeFields)

					.addBlock (
						this::writeCollections)

					.addBlock (
						this::writeEquals)

				;

				if (modelMeta.type ().record ()) {

					modelWriter

						.addBlock (
							this::writeCompareTo)

						.addBlock (
							this::writeToString)

					;

				}

				classUnitWriter.addBlock (
					modelWriter);

				if (taskLogger.errors ()) {
					return;
				}

				classUnitWriter.write (
					taskLogger);

				if (taskLogger.errors ()) {
					return;
				}

				formatWriter.commit ();

			}

		}

	}

	public final static
	Map <ModelMetaType, Class <?>> modelInterfacesByType =
		ImmutableMap.<ModelMetaType, Class <?>> builder ()

		.put (
			ModelMetaType.common,
			CommonRecord.class)

		.put (
			ModelMetaType.composite,
			RecordComponent.class)

		.put (
			ModelMetaType.ephemeral,
			EphemeralRecord.class)

		.put (
			ModelMetaType.event,
			EventRecord.class)

		.put (
			ModelMetaType.major,
			MajorRecord.class)

		.put (
			ModelMetaType.minor,
			MinorRecord.class)

		.put (
			ModelMetaType.root,
			RootRecord.class)

		.put (
			ModelMetaType.type,
			TypeRecord.class)

		.build ();

	public
	void writeConstructor (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// comment

		formatWriter.writeLineFormat (
			"// constructor");

		formatWriter.writeNewline ();

		// open function

		if (modelMeta.type ().record ()) {

			formatWriter.writeLineFormat (
				"@Deprecated");

		}

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"%s () {",
			recordClassName);

		formatWriter.writeNewline ();

		// close function

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	private
	void writeInternalFields (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeFields");

		) {

			formatWriter.writeLineFormat (
				"// internal fields");

			formatWriter.writeNewline ();

			javaPropertyWriterProvider.provide (
				taskLogger)

				.thisClassName (
					recordClassName)

				.typeClass (
					Boolean.class)

				.propertyName (
					recordUpdatedFieldName)

				.defaultValue (
					"false")

				.writeBlock (
					taskLogger,
					imports,
					formatWriter)

			;

		}

	}

	private
	void writeFields (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeFields");

		) {

			if (modelMeta.fields ().isEmpty ()) {
				return;
			}

			formatWriter.writeLineFormat (
				"// fields");

			formatWriter.writeNewline ();

			ModelFieldWriterContext nextContext =
				new ModelFieldWriterContext ()

				.modelMeta (
					modelMeta)

				.recordClassName (
					recordClassName)

			;

			ModelFieldWriterTarget nextTarget =
				new ModelFieldWriterTarget ()

				.imports (
					imports)

				.formatWriter (
					formatWriter)

			;

			modelWriterBuilder.write (
				taskLogger,
				nextContext,
				modelMeta.fields (),
				nextTarget);

		}

	}

	private
	void writeCollections (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeCollections");

		) {

			if (modelMeta.collections ().isEmpty ()) {
				return;
			}

			formatWriter.writeLineFormat (
				"// collections");

			formatWriter.writeNewline ();

			ModelFieldWriterContext nextContext =
				new ModelFieldWriterContext ()

				.modelMeta (
					modelMeta)

				.recordClassName (
					recordClassName);

			ModelFieldWriterTarget nextTarget =
				new ModelFieldWriterTarget ()

				.imports (
					imports)

				.formatWriter (
					formatWriter);

			modelWriterBuilder.write (
				taskLogger,
				nextContext,
				modelMeta.collections (),
				nextTarget);

		}

	}

	private
	void writeEquals (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write comment

		formatWriter.writeLineFormat (
			"// equals");

		formatWriter.writeNewline ();

		// write override annotation

		formatWriter.writeLineFormat (
			"@Override");

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"boolean equals (");

		formatWriter.writeLineFormat (
			"\t\t%s otherObject) {",
			imports.register (
				Object.class));

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write class comparison

		formatWriter.writeLineFormat (
			"if (! (otherObject instanceof %s)) {",
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeLineFormat (
			"\treturn false;");

		formatWriter.writeLineFormat (
			"}\n");

		formatWriter.writeNewline ();

		// write cast

		formatWriter.writeLineFormat (
			"%s other =",
			recordClassName);

		formatWriter.writeLineFormat (
			"\t(%s)",
			recordClassName);

		formatWriter.writeLineFormat (
			"\totherObject;");

		formatWriter.writeNewline ();

		if (modelMeta.type ().record ()) {

			// check for null id

			formatWriter.writeLineFormat (
				"if (getId () == null || other.getId () == null) {");

			formatWriter.writeLineFormat (
				"\treturn false;");

			formatWriter.writeLineFormat (
				"}");

			formatWriter.writeNewline ();

			// compare id

			formatWriter.writeLineFormat (
				"return getId () == other.getId ();");

			formatWriter.writeNewline ();

		} else if (modelMeta.type ().composite ()) {

			// TODO this is not nice at all

			for (
				ModelFieldSpec modelField
					: modelMeta.fields ()
			) {

				String fieldName;

				if (modelField instanceof ReferenceFieldSpec) {

					ReferenceFieldSpec referenceField =
						(ReferenceFieldSpec)
						modelField;

					fieldName =
						ifNull (
							referenceField.name (),
							referenceField.typeName ());

				} else if (modelField instanceof DateFieldSpec) {

					DateFieldSpec dateField =
						(DateFieldSpec)
						modelField;

					fieldName =
						dateField.name ();

				} else if (modelField instanceof IntegerFieldSpec) {

					IntegerFieldSpec integerField =
						(IntegerFieldSpec)
						modelField;

					fieldName =
						integerField.name ();

				} else {

					throw new RuntimeException (
						stringFormat (
							"Don't know how to get name of component field ",
							"type %s",
							modelField.getClass ().getSimpleName ()));

				}

				formatWriter.writeLineFormat (
					"if (");

				formatWriter.writeLineFormat (
					"\t! (");

				formatWriter.writeLineFormat (
					"\t\tget%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t&& other.get%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t) && (");

				formatWriter.writeLineFormat (
					"\t\tget%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t|| other.get%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t|| ! get%s ().equals (",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t\tother.get%s ())",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t)");

				formatWriter.writeLineFormat (
					") {");

				formatWriter.writeLineFormat (
					"\treturn false;");

				formatWriter.writeLineFormat (
					"}");

				formatWriter.writeNewline ();

			}

			formatWriter.writeLineFormat (
				"return true;");

			formatWriter.writeNewline ();

		} else {

			throw new RuntimeException ();

		}

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	void writeCompareTo (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// TODO generate better code

		// write comment

		formatWriter.writeLineFormat (
			"// compare to");

		formatWriter.writeNewline ();

		// write override annotation

		formatWriter.writeLineFormat (
			"@Override");

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"int compareTo (");

		formatWriter.writeLineFormat (
			"\t\t%s <%s> otherRecord) {",
			imports.register (
				Record.class),
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write cast to concrete type

		formatWriter.writeLineFormat (
			"%s other =",
			recordClassName);

		formatWriter.writeLineFormat (
			"\t(%s) otherRecord;",
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeNewline ();

		// create compare to builder

		formatWriter.writeLineFormat (
			"return new %s ()",
			imports.register (
				CompareToBuilder.class));

		formatWriter.writeNewline ();

		// scan fields

		ParentFieldSpec parentField = null;
		ParentTypeFieldSpec parentTypeField = null;
		ParentIdFieldSpec parentIdField = null;
		MasterFieldSpec masterField = null;

		TypeCodeFieldSpec typeCodeField = null;
		CodeFieldSpec codeField = null;
		IndexFieldSpec indexField = null;

		List <IdentityReferenceFieldSpec> identityReferenceFields =
			new ArrayList<> ();

		List <IdentityIntegerFieldSpec> identityIntegerFields =
			new ArrayList<> ();

		List <IdentityStringFieldSpec> identityStringFields =
			new ArrayList<> ();

		TimestampFieldSpec timestampField = null;

		boolean gotName = false;

		for (
			ModelFieldSpec modelField
				: modelMeta.fields ()
		) {

			if (modelField instanceof ParentFieldSpec) {

				parentField =
					(ParentFieldSpec)
					modelField;

			}

			if (modelField instanceof ParentTypeFieldSpec) {

				parentTypeField =
					(ParentTypeFieldSpec)
					modelField;

			}

			if (modelField instanceof ParentIdFieldSpec) {

				parentIdField =
					(ParentIdFieldSpec)
					modelField;

			}

			if (modelField instanceof MasterFieldSpec) {

				masterField =
					(MasterFieldSpec)
					modelField;

			}

			if (modelField instanceof TypeCodeFieldSpec) {

				typeCodeField =
					(TypeCodeFieldSpec)
					modelField;

			}

			if (modelField instanceof CodeFieldSpec) {

				codeField =
					(CodeFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IndexFieldSpec) {

				indexField =
					(IndexFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IdentityReferenceFieldSpec) {

				identityReferenceFields.add (
					(IdentityReferenceFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityIntegerFieldSpec) {

				identityIntegerFields.add (
					(IdentityIntegerFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityStringFieldSpec) {

				identityStringFields.add (
					(IdentityStringFieldSpec)
					modelField);

				gotName = true;

			}

			if (
				modelField instanceof TimestampFieldSpec
				&& timestampField == null
			) {

				timestampField =
					(TimestampFieldSpec)
					modelField;

			}

		}

		// write comparisons

		if (modelMeta.type () == ModelMetaType.event) {

			if (timestampField == null) {
				throw new RuntimeException ();
			}

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.get%s (),",
				capitalise (
					timestampField.name ()));

			formatWriter.writeLineFormat (
				"\t\tget%s ())",
				capitalise (
					timestampField.name ()));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.getId (),");

			formatWriter.writeLineFormat (
				"\t\tgetId ())");

			formatWriter.writeNewline ();

		} else if (gotName || masterField != null) {

			if (parentField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				formatWriter.writeNewline ();

			}

			if (parentTypeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				formatWriter.writeNewline ();

			}

			if (parentIdField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tgetParentId (),");

				formatWriter.writeLineFormat (
					"\t\tother.getParentId ())");

				formatWriter.writeNewline ();

			}

			if (masterField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				formatWriter.writeNewline ();

			}

			if (typeCodeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				formatWriter.writeNewline ();

			}

			if (codeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				formatWriter.writeNewline ();

			}

			if (indexField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				formatWriter.writeNewline ();

			}

			for (
				IdentityReferenceFieldSpec identityReferenceField
					: identityReferenceFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				formatWriter.writeNewline ();

			}

			for (
				IdentityIntegerFieldSpec identityIntegerField
					: identityIntegerFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						identityIntegerField.name ()));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						identityIntegerField.name ()));

				formatWriter.writeNewline ();

			}

			for (
				IdentityStringFieldSpec identityStringField
					: identityStringFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						identityStringField.name ()));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						identityStringField.name ()));

				formatWriter.writeNewline ();

			}

		} else {

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.getId (),");

			formatWriter.writeLineFormat (
				"\t\tgetId ())");

			formatWriter.writeNewline ();

		}

		// write converstion to return value

		formatWriter.writeLineFormat (
			"\t.toComparison ();");

		formatWriter.writeNewline ();

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	public
	void writeToString (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write comment

		formatWriter.writeLineFormat (
			"// to string");

		formatWriter.writeNewline ();

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"String toString () {");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write function body

		if (
			stringEqualSafe (
				modelMeta.name (),
				"text")
		) {

			formatWriter.writeLineFormat (
				"return getText ();");

		} else {

			formatWriter.writeLineFormat (
				"return \"%s (\" + getId () + \")\";",
				recordClassName);

		}

		formatWriter.writeNewline ();

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	// data

	public final static
	String recordUpdatedFieldName =
		"recordUpdated";

}
