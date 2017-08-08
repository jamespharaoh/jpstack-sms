package wbs.console.helper.generate;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listSlice;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.classPackageName;
import static wbs.utils.io.FileUtils.directoryCreateWithParents;
import static wbs.utils.io.FileUtils.fileExistsFormat;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.forms.object.EntityFinder;
import wbs.console.helper.core.ConsoleHelperImplementation;
import wbs.console.helper.core.ConsoleHelperMethods;
import wbs.console.helper.core.ConsoleHooks;
import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.lookup.ObjectLookup;

import wbs.framework.codegen.JavaAssignmentWriter;
import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaClassWriter;
import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.generate.ObjectHelperGenerator;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelperMethods;
import wbs.framework.object.ObjectModel;
import wbs.framework.object.ObjectModelMethods;
import wbs.framework.object.ObjectTypeRegistry;

import wbs.utils.string.AtomicFileWriter;
import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("consoleHelperGenerator")
public
class ConsoleHelperGenerator {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JavaAssignmentWriter> javaAssignmentWriterProvider;

	@PrototypeDependency
	ComponentProvider <JavaClassWriter> javaClassWriterProvider;

	@PrototypeDependency
	ComponentProvider <JavaClassUnitWriter> javaClassUnitWriterProvider;

	// properties

	@Getter @Setter
	Model <?> model;

	// state

	String filename;
	JavaClassWriter classWriter;

	Model <?> parentModel;

	String packageName;
	String recordClassName;
	String objectHelperInterfaceName;
	String objectHelperImplementationName;

	String consoleHelperInterfaceName;
	String consoleHelperImplementationName;

	boolean hasDao;
	String daoMethodsInterfaceName;
	String daoComponentName;
	String daoImplementationClassName;
	Class <?> daoMethodsInterface;

	boolean hasExtra;
	String extraMethodsInterfaceName;
	String extraComponentName;
	String extraImplementationClassName;
	Class <?> extraMethodsInterface;

	boolean hasHooks;
	String consoleHooksClassName;
	String consoleHooksComponentName;

	//Class <?> consoleHelperInterface;

	FormatWriter javaWriter;

	Set <Pair <String, List <Class <?>>>> delegatedMethods =
		new HashSet<> ();

	// implementation

	public
	void generateHelper (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"generateHelper");

		) {

			init ();

			writeClass (
				taskLogger);

		}

	}

	private
	void init () {

		if (model.parentTypeIsFixed ()) {

			parentModel =
				ifThenElse (
					model.isRooted (),

				() -> entityHelper.recordModelsByName ().get (
					"root"),

				() -> ifThenElse (
					model.parentTypeIsFixed ()
					&& ! model.isRoot (),

					() -> entityHelper.recordModelsByClass ().get (
						model.parentField ().valueType ()),

					() -> null

				)

			);

		}

		List <String> modelPackageNameParts =
			stringSplitFullStop (
				classPackageName (
					model.objectClass ()));

		packageName =
			joinWithFullStop (
				listSlice (
					modelPackageNameParts,
					0l,
					collectionSize (
						modelPackageNameParts) - 1));

		recordClassName =
			stringFormat (
				"%sRec",
				capitalise (
					model.objectName ()));

		objectHelperInterfaceName =
			stringFormat (
				"%sObjectHelper",
				capitalise (
					model.objectName ()));

		objectHelperImplementationName =
			stringFormat (
				"%sObjectHelperImplementation",
				capitalise (
					model.objectName ()));

		consoleHelperInterfaceName =
			stringFormat (
				"%sConsoleHelper",
				capitalise (
					model.objectName ()));

		consoleHelperImplementationName =
			stringFormat (
				"%sConsoleHelperImplementation",
				capitalise (
					model.objectName ()));

		// dao

		daoMethodsInterfaceName =
			stringFormat (
				"%sDaoMethods",
				capitalise (
					model.objectName ()));

		Optional <Class <?>> daoMethodsInterfaceOptional =
			classForName (
				packageName + ".model",
				daoMethodsInterfaceName);

		if (
			optionalIsPresent (
				daoMethodsInterfaceOptional)
		) {

			hasDao =
				true;

			daoMethodsInterface =
				optionalGetRequired (
					daoMethodsInterfaceOptional);

			daoComponentName =
				stringFormat (
					"%sDao",
					uncapitalise (
						model.objectName ()));

			daoImplementationClassName =
				stringFormat (
					"%s.hibernate.%sDaoHibernate",
					packageName,
					capitalise (
						model.objectName ()));

		} else {

			hasDao =
				false;

		}

		// extra

		extraMethodsInterfaceName =
			stringFormat (
				"%sObjectHelperMethods",
				capitalise (
					model.objectName ()));

		Optional <Class <?>> extraMethodsInterfaceOptional =
			classForName (
				packageName + ".model",
				extraMethodsInterfaceName);

		if (
			optionalIsPresent (
				extraMethodsInterfaceOptional)
		) {

			hasExtra =
				true;

			extraMethodsInterface =
				optionalGetRequired (
					extraMethodsInterfaceOptional);

			extraComponentName =
				stringFormat (
					"%sObjectHelperMethodsImplementation",
					uncapitalise (
						model.objectName ()));

			extraImplementationClassName =
				stringFormat (
					"%s.logic.%sObjectHelperMethodsImplementation",
					packageName,
					capitalise (
						model.objectName ()));

		} else {

			hasExtra =
				false;

		}

		// hooks

		consoleHooksClassName =
			stringFormat (
				"%s.console.%sConsoleHooks",
				packageName,
				capitalise (
					model.objectName ()));

		consoleHooksComponentName =
			stringFormat (
				"%sConsoleHooks",
				model.objectName ());

		if (
			fileExistsFormat (
				"src/%s.java",
				stringReplaceAllSimple (
					".",
					"/",
					consoleHooksClassName))
		) {

			hasHooks =
				true;

		} else {

			hasHooks =
				false;

		}

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/console",
				packageName.replace ('.', '/'));

		directoryCreateWithParents (
			directory);

		filename =
			stringFormat (
				"%s/%s.java",
				directory,
				consoleHelperImplementationName);

	}

	private
	void writeClass (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeClass");

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
					"%s.console",
					packageName);

			classWriter =
				javaClassWriterProvider.provide (
					taskLogger)

				.className (
					consoleHelperImplementationName)

				/*
				.addClassAnnotation (
					new JavaAnnotationWriter ()

					.name (
						"java.lang.SuppressWarnings"))

					.addAttributeFormat (
						"value",
						"{ \"rawtypes\", \"unchecked\" }"))
				*/

				.addClassModifier (
					"public")

				.addImplements (
					imports ->
						stringFormat (
							"%s <%s>",
							imports.register (
								ObjectHelperMethods.class),
							imports.registerFormat (
								"%s.model.%s",
								packageName,
								recordClassName)))

				.addImplementsFormat (
					"%s.console.%s",
					packageName,
					consoleHelperInterfaceName)

				.addTypeParameterMapping (
					"RecordType",
					model.objectClass ().getSimpleName ())

				.addTypeParameterMapping (
					"EntityType",
					model.objectClass ().getSimpleName ())

				.addTypeParameterMapping (
					"ObjectType",
					model.objectClass ().getSimpleName ());

			addSingletonDependencies ();
			addPrototypeDependencies ();

			addState ();

			classWriter.addBlock (
				this::writeLifecycle);

			addDelegations ();

			classUnitWriter.addBlock (
				classWriter);

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

	void addSingletonDependencies () {

		classWriter.addSingletonDependency (
			ComponentManager.class);

		classWriter.addSingletonDependency (
			EntityHelper.class);

		classWriter.addClassSingletonDependency (
			LogContext.class);

		classWriter.addSingletonDependency (
			ObjectTypeRegistry.class);

		classWriter.addNamedSingletonDependency (
			ConsoleHelperProvider.class,
			stringFormat (
				"%hConsoleHelperProvider",
				model.objectName ()));

		classWriter.addNamedSingletonDependency (
			stringFormat (
				"%s.logic.%s",
				packageName,
				objectHelperImplementationName),
			stringFormat (
				"%sObjectHelper",
				model.objectName ()));

		if (hasDao) {

			classWriter.addNamedSingletonDependency (
				daoImplementationClassName,
				daoComponentName);

		}

		if (hasExtra) {

			classWriter.addNamedSingletonDependency (
				extraImplementationClassName,
				extraComponentName);

		}

		if (hasHooks) {

			classWriter.addNamedSingletonDependency (
				consoleHooksClassName,
				consoleHooksComponentName);

		}

	}

	void addPrototypeDependencies () {

		classWriter.addPrototypeDependency (
			imports ->
				stringFormat (
					"%s <%s>",
					imports.register (
						ConsoleHelperImplementation.class),
					imports.registerFormat (
						"%s.model.%s",
						packageName,
						recordClassName)),
			"consoleHelperImplementation");

	}

	void addState () {

		classWriter.addState (
			Model.class,
			"parentModel");

		classWriter.addState (
			ObjectModel.class,
			"objectModel");

		classWriter.addState (
			ConsoleHooks.class,
			"consoleHooksImplementation");

		ObjectHelperGenerator.componentNames.forEach (
			componentName ->
				classWriter.addState (
					imports ->
						stringFormat (
							"%s <%s>",
							imports.registerFormat (
								"wbs.framework.object.",
								"ObjectHelper%sImplementation",
								capitalise (
									componentName)),
							imports.register (
								model.objectClass ())),
					stringFormat (
						"%sImplementation",
						componentName)));

		classWriter.addState (
			imports ->
				stringFormat (
					"%s <%s>",
					imports.register (
						ConsoleHelperImplementation.class),
					imports.register (
						stringFormat (
							"%s.model.%s",
							packageName,
							recordClassName))),
			"consoleHelperImplementation");

	}

	void writeLifecycle (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeLifecycle");

		) {

			formatWriter.writeLineFormat (
				"// lifecycle");

			formatWriter.writeNewline ();

			// open method

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					NormalLifecycleSetup.class));

			formatWriter.writeLineFormat (
				"public");

			formatWriter.writeLineFormat (
				"void setup (");

			formatWriter.writeLineFormat (
				"\t\t%s parentTaskLogger) {",
				imports.register (
					TaskLogger.class));

			formatWriter.writeNewline ();

			formatWriter.increaseIndent ();

			// nest task logger

			formatWriter.writeLineFormat (
				"TaskLogger taskLogger =");

			formatWriter.writeLineFormat (
				"\tlogContext.nestTaskLogger (");

			formatWriter.writeLineFormat (
				"\t\tparentTaskLogger,");

			formatWriter.writeLineFormat (
				"\t\t\"setup\");");

			formatWriter.writeNewline ();

			// object model

			formatWriter.writeLineFormat (
				"objectModel =");

			formatWriter.writeLineFormat (
				"\t%sObjectHelper.objectModel ();",
				model.objectName ());

			formatWriter.writeNewline ();

			// console hooks

			formatWriter.writeLineFormat (
				"consoleHooksImplementation =");

			if (hasHooks) {

				formatWriter.writeLineFormat (
					"\t%s;",
					consoleHooksComponentName);

			} else {

				formatWriter.writeLineFormat (
					"\tnew %s.DefaultImplementation ();",
					imports.register (
						ConsoleHooks.class));

			}

			// console helper implementation

			javaAssignmentWriterProvider.provide (
				taskLogger)

				.variableName (
					"consoleHelperImplementation")

				.provider (
					"consoleHelperImplementationProvider")

				.propertyFormat (
					"objectHelper",
					"%sObjectHelper",
					model.objectName ())

				.propertyFormat (
					"consoleHelperProvider",
					"%sConsoleHelperProvider",
					model.objectName ())

				.property (
					"consoleHooks",
					"consoleHooksImplementation")

				.write (
					formatWriter);

			// components

			ObjectHelperGenerator.componentNames.forEach (
				componentName -> {

				formatWriter.writeLineFormat (
					"%sImplementation =",
					componentName);

				formatWriter.writeLineFormat (
					"\t%sObjectHelper.%sImplementation ();",
					model.objectName (),
					componentName);

				formatWriter.writeNewline ();

			});

			// close method

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"}");

			formatWriter.writeNewline ();

		}

	}

	private
	void addDelegations () {

		if (hasExtra) {

			classWriter.addDelegation (
				extraMethodsInterface,
				extraComponentName);

		}

		if (hasDao) {

			classWriter.addDelegation (
				daoMethodsInterface,
				daoComponentName);

		}

		classWriter.addDelegation (
			ConsoleHelperMethods.class,
			"consoleHelperImplementation");

		classWriter.addDelegation (
			EntityFinder.class,
			"consoleHelperImplementation");

		classWriter.addDelegation (
			ObjectLookup.class,
			"consoleHelperImplementation");

		for (
			Map.Entry <String, Class <?>> componentEntry
				: ObjectHelperGenerator.componentInterfacesByName.entrySet ()
		) {

			String componentName =
				componentEntry.getKey ();

			Class <?> componentClass =
				componentEntry.getValue ();

			classWriter.addDelegation (
				componentClass,
				stringFormat (
					"%sImplementation",
					componentName));

		}

		classWriter.addDelegation (
			ObjectModelMethods.class,
			"objectModel");

	}

}
