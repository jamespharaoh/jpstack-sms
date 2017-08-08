package wbs.framework.codegen;

import static wbs.utils.collection.ArrayUtils.arrayIsNotEmpty;
import static wbs.utils.collection.ArrayUtils.arrayMap;
import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.CollectionUtils.listSlice;
import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.fullClassName;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.ReflectionUtils.methodInvokeByName;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.parameterSourceTypeName;
import static wbs.utils.etc.TypeUtils.typeSourceName;
import static wbs.utils.etc.TypeUtils.typeVariableSourceDeclaration;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringDoesNotContain;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("javaClassWriter")
public
class JavaClassWriter
	implements JavaBlockWriter {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String className;

	@Getter @Setter
	List <JavaAnnotationWriter> classAnnotations =
		new ArrayList<> ();

	@Getter @Setter
	List <String> classModifiers =
		new ArrayList<> ();

	@Getter @Setter
	String extendsClassName;

	@Getter @Setter
	List <Function <JavaImportRegistry, String>> implementsInterfaces =
		new ArrayList<> ();

	@Getter @Setter
	List <JavaBlockWriter> blocks =
		new ArrayList<> ();

	@Getter @Setter
	List <Dependency> singletonDependencies =
		new ArrayList<> ();

	@Getter @Setter
	List <Dependency> prototypeDependencies =
		new ArrayList<> ();

	@Getter @Setter
	List <State> states =
		new ArrayList<> ();

	@Getter @Setter
	Map <String, String> typeParameterMappings =
		new HashMap<> ();

	@Getter @Setter
	List <Pair <Class <?>, String>> delegations =
		new ArrayList<> ();

	// setters and getters

	public
	JavaClassWriter classNameFormat (
			@NonNull String ... arguments) {

		return className (
			stringFormatArray (
				arguments));

	}

	public
	JavaClassWriter addClassAnnotation (
			@NonNull JavaAnnotationWriter annotation) {

		classAnnotations.add (
			annotation);

		return this;

	}

	public
	JavaClassWriter addClassModifier (
			@NonNull String modifier) {

		classModifiers.add (
			modifier);

		return this;

	}

	public
	JavaClassWriter addImplements (
			@NonNull Function <JavaImportRegistry, String> value) {

		implementsInterfaces.add (
			value);

		return this;

	}

	public
	JavaClassWriter addImplementsName (
			@NonNull String interfaceName,
			@NonNull List <String> parameters) {

		if (
			stringDoesNotContain (
				".",
				interfaceName)
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"Not a fully qualified class name: %s",
					interfaceName));

		}

		if (
			collectionIsNotEmpty (
				parameters)
		) {

			return addImplements (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							interfaceName),
						joinWithCommaAndSpace (
							iterableMap (
								parameters,
								parameter ->
									imports.register (
										parameter)))));

		} else {

			return addImplements (
				imports ->
					imports.register (
						interfaceName));

		}

	}

	public
	JavaClassWriter addImplementsFormat (
			@NonNull String ... arguments) {

		return addImplementsName (
			stringFormatArray (
				arguments),
			emptyList ());

	}

	public
	JavaClassWriter addImplementsClass (
			@NonNull Class <?> interfaceClass) {

		return addImplementsName (
			interfaceClass.getName (),
			emptyList ());

	}

	public
	JavaClassWriter addBlock (
			@NonNull JavaBlockWriter block) {

		blocks.add (
			block);

		return this;

	}

	// singleton dependencies

	public
	JavaClassWriter addSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName,
			@NonNull Class <?> annotationClass,
			@NonNull Boolean named) {

		singletonDependencies.add (
			new Dependency ()

			.annotationClass (
				annotationClass)

			.classNameSupplier (
				imports ->
					imports.register (
						typeName))

			.memberName (
				variableName)

			.provider (
				false)

			.named (
				named)

		);

		return this;

	}

	public
	JavaClassWriter addSingletonDependency (
			@NonNull Class <?> dependencyClass,
			@NonNull Class <?> annotationClass,
			@NonNull Boolean named) {

		SingletonComponent singletonComponentAnnotation =
			(SingletonComponent)
			dependencyClass.getAnnotation (
				SingletonComponent.class);

		String variableName;

		if (
			isNotNull (
				singletonComponentAnnotation)
		) {

			variableName =
				singletonComponentAnnotation.value ();

		} else {

			variableName =
				uncapitalise (
					dependencyClass.getSimpleName ());

		}

		return addSingletonDependency (
			classNameFull (
				dependencyClass),
			variableName,
			annotationClass,
			named);

	}

	public
	JavaClassWriter addSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName,
			@NonNull Class <?> annotationClass) {

		return addSingletonDependency (
			typeName,
			variableName,
			annotationClass,
			false);

	}

	public
	JavaClassWriter addNamedSingletonDependency (
			@NonNull Class <?> typeClass,
			@NonNull String variableName) {

		return addSingletonDependency (
			classNameFull (
				typeClass),
			variableName,
			SingletonDependency.class,
			true);

	}

	public
	JavaClassWriter addNamedSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addSingletonDependency (
			typeName,
			variableName,
			SingletonDependency.class,
			true);

	}

	public
	JavaClassWriter addSingletonDependency (
			@NonNull Class <?> dependencyClass) {

		return addSingletonDependency (
			dependencyClass,
			SingletonDependency.class,
			false);

	}

	public
	JavaClassWriter addClassSingletonDependency (
			@NonNull Class <?> dependencyClass) {

		return addSingletonDependency (
			fullClassName (
				dependencyClass),
			uncapitalise (
				classNameSimple (
					dependencyClass)),
			ClassSingletonDependency.class,
			false);

	}

	// prototype dependencies

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull Function <JavaImportRegistry, String> classNameSupplier,
			@NonNull String variableName,
			@NonNull Boolean named) {

		prototypeDependencies.add (
			new Dependency ()

			.annotationClass (
				PrototypeDependency.class)

			.classNameSupplier (
				classNameSupplier)

			.memberName (
				variableName)

			.provider (
				true)

			.named (
				named)

		);

		return this;

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addPrototypeDependency (
			imports ->
				imports.register (
					typeName),
			variableName,
			false);

	}

	public
	JavaClassWriter addNamedPrototypeDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addPrototypeDependency (
			imports ->
				imports.register (
					typeName),
			variableName,
			true);

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull Class <?> dependencyClass) {

		return addPrototypeDependency (
			imports ->
				imports.register (
					dependencyClass),
			variableNameForDependency (
				PrototypeComponent.class,
				dependencyClass),
			false);

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull Function <JavaImportRegistry, String> classNameSupplier,
			@NonNull String variableName) {

		return addPrototypeDependency (
			classNameSupplier,
			variableName,
			false);

	}

	// ==================== addState

	public
	JavaClassWriter addState (
			@NonNull Function <JavaImportRegistry, String> typeNameSupplier,
			@NonNull String memberName,
			@NonNull Boolean getter,
			@NonNull Boolean setter) {

		states.add (
			new State ()

			.typeNameSupplier (
				typeNameSupplier)

			.memberName (
				memberName)

			.getter (
				getter)

			.setter (
				setter)

		);

		return this;

	}

	public
	JavaClassWriter addState (
			@NonNull Function <JavaImportRegistry, String> typeNameSupplier,
			@NonNull String memberName) {

		return addState (
			typeNameSupplier,
			memberName,
			false,
			false);

	}

	public
	JavaClassWriter addState (
			@NonNull Class <?> classObject,
			@NonNull String memberName,
			@NonNull Boolean getter,
			@NonNull Boolean setter) {

		return addState (
			imports ->
				imports.register (
					classObject),
			memberName,
			getter,
			setter);

	}

	public
	JavaClassWriter addState (
			@NonNull Class <?> classObject,
			@NonNull String memberName) {

		return addState (
			imports ->
				imports.register (
					classObject),
			memberName,
			false,
			false);

	}

	public
	JavaClassWriter addState (
			@NonNull String typeName,
			@NonNull String memberName,
			@NonNull Boolean getter,
			@NonNull Boolean setter) {

		return addState (
			imports ->
				imports.register (
					typeName),
			memberName,
			getter,
			setter);

	}

	public
	JavaClassWriter addState (
			@NonNull String typeName,
			@NonNull String memberName) {

		return addState (
			imports ->
				imports.register (
					typeName),
			memberName,
			false,
			false);

	}

	public
	JavaClassWriter addTypeParameterMapping (
			@NonNull String parameterName,
			@NonNull String parameterValue) {

		typeParameterMappings.put (
			parameterName,
			parameterValue);

		return this;

	}

	public
	JavaClassWriter addDelegation (
			@NonNull Class <?> delegateInterface,
			@NonNull String delegateName) {

		delegations.add (
			Pair.of (
				delegateInterface,
				delegateName));

		return this;

	}

	// implementation

	@Override
	public
	void writeBlock (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeBlock");

		) {

			// class annotations

			for (
				JavaAnnotationWriter annotationWriter
					: classAnnotations
			) {

				annotationWriter.writeAnnotation (
					imports,
					formatWriter);

			}

			// class modifiers

			if (
				collectionIsNotEmpty (
					classModifiers)
			) {

				formatWriter.writeLineFormat (
					"%s",
					joinWithSpace (
						classModifiers));

			}

			// class declaration

			if (

				isNull (
					extendsClassName)

				&& collectionIsEmpty (
					implementsInterfaces)

			) {

				formatWriter.writeLineFormat (
					"class %s {",
					className);

			} else {

				formatWriter.writeLineFormat (
					"class %s",
					className);

			}

			// class extends declaration

			if (
				isNotNull (
					extendsClassName)
			) {

				if (
					collectionIsEmpty (
						implementsInterfaces)
				) {

					formatWriter.writeLineFormat (
						"\textends %s {",
						imports.register (
							extendsClassName));

				} else {

					formatWriter.writeLineFormat (
						"\textends %s",
						imports.register (
							extendsClassName));

				}

			}

			// class implements declaration

			if (
				collectionIsNotEmpty (
					implementsInterfaces)
			) {

				if (
					collectionHasOneItem (
						implementsInterfaces)
				) {

					Function <JavaImportRegistry, String> implementsInterface =
						listFirstElementRequired (
							implementsInterfaces);

					formatWriter.writeLineFormat (
						"\timplements %s {",
						implementsInterface.apply (
							imports));

				} else {

					formatWriter.writeLineFormat (
						"\timplements");

					for (
						Function <JavaImportRegistry, String> implementsInterface
							: listSliceAllButLastItemRequired (
								implementsInterfaces)
					) {

						formatWriter.writeLineFormat (
							"\t\t%s,",
							implementsInterface.apply (
								imports));

					}

					Function <JavaImportRegistry, String> implementsInterface =
						listLastItemRequired (
							implementsInterfaces);

					formatWriter.writeLineFormat (
						"\t\t%s {",
						implementsInterface.apply (
							imports));

				}

			}

			formatWriter.writeNewline ();

			// class body

			formatWriter.increaseIndent ();

			writeLogger (
				imports,
				formatWriter);

			writeDependencies (
				imports,
				formatWriter,
				"singleton dependencies",
				singletonDependencies);

			writeDependencies (
				imports,
				formatWriter,
				"prototype dependencies",
				prototypeDependencies);

			writeState (
				imports,
				formatWriter);

			blocks.forEach (
				block ->
					block.writeBlock (
						taskLogger,
						imports,
						formatWriter));

			writeDelegations (
				imports,
				formatWriter);

			formatWriter.decreaseIndent ();

			// end class

			formatWriter.writeLineFormat (
				"}");

		}

	}

	public
	JavaClassWriter writeLogger (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// logger");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"%s logger =",
			imports.register (
				Logger.class));

		formatWriter.writeLineFormat (
			"\t%s.getLogger (",
			imports.register (
				LogManager.class));

		formatWriter.writeLineFormat (
			"\t\t%s.class);",
			className);

		formatWriter.writeNewline ();

		return this;

	}

	public
	JavaClassWriter writeDependencies (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter,
			@NonNull String comment,
			@NonNull List <Dependency> dependencies) {

		if (
			collectionIsEmpty (
				dependencies)
		) {
			return this;
		}

		formatWriter.writeLineFormat (
			"// %s",
			comment);

		formatWriter.writeNewline ();

		for (
			Dependency dependency
				: dependencies
		) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					dependency.annotationClass ()));

			if (dependency.named ()) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					NamedDependency.class));

			}

			if (dependency.provider ()) {

				formatWriter.writeLineFormat (
					"%s <%s> %sProvider;",
					imports.register (
						ComponentProvider.class),
					dependency.classNameSupplier ().apply (
						imports),
					dependency.memberName);

			} else {

				formatWriter.writeLineFormat (
					"%s %s;",
					dependency.classNameSupplier ().apply (
						imports),
					dependency.memberName);

			}

			formatWriter.writeNewline ();

		}

		return this;

	}

	public
	JavaClassWriter writeState (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (
			collectionIsEmpty (
				states)
		) {
			return this;
		}

		formatWriter.writeLineFormat (
			"// state");

		formatWriter.writeNewline ();

		for (
			State state
				: states
		) {

			formatWriter.writeLineFormat (
				"%s %s;",
				state.typeNameSupplier.apply (
					imports),
				state.memberName ());

		}

		formatWriter.writeNewline ();

		for (
			State state
				: states
		) {

			if (state.getter ()) {

				formatWriter.writeLineFormat (
					"public");

				formatWriter.writeLineFormat (
					"%s %s () {",
					state.typeNameSupplier.apply (
						imports),
					state.memberName ());

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"\treturn %s;",
					state.memberName ());

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"}");

				formatWriter.writeNewline ();

			}

			if (state.setter ()) {

				formatWriter.writeLineFormat (
					"public");

				formatWriter.writeLineFormat (
					"%s %s (",
					className,
					state.memberName ());

				formatWriter.writeLineFormat (
					"\t%s %s) {",
					state.typeNameSupplier.apply (
						imports),
					state.memberName ());

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"\tthis.%s =",
					state.memberName ());

				formatWriter.writeLineFormat (
					"\t\t%s;",
					state.memberName ());

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"return this");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"}");

				formatWriter.writeNewline ();

			}

		}

		return this;

	}

	void writeDelegations (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		Set <Pair <String, List <Class <?>>>> delegatedMethods =
			new HashSet<> ();

		for (
			Pair <Class <?>, String> delegate
				: delegations
		) {

			Class <?> delegateInterface =
				delegate.getLeft ();

			String delegateName =
				delegate.getRight ();

			formatWriter.writeLineFormat (
				"// delegate %s",
				camelToSpaces (
					uncapitalise (
						delegateInterface.getSimpleName ())));

			formatWriter.writeNewline ();

			// sort method names efficiently

			List <Method> sortedMethods =
				Arrays.stream (
					delegateInterface.getMethods ())

				.filter (method ->

					isNull (
						method.getAnnotation (
							DoNotDelegate.class))

					&& ! method.isDefault ()

					&& ! method.isSynthetic ()

				)

				.map (
					method -> Pair.of (
						stringFormat (
							"%s (%s)",
							method.getName (),
							joinWithCommaAndSpace (
								arrayMap (
									Class::getName,
									method.getParameterTypes ()))),
						method))

				.sorted (
					(left, right) ->
						left.getKey ().compareTo (
							right.getKey ()))

				.map (
					Pair::getValue)

				.collect (
					Collectors.toList ());

			// iterate methods

			for (
				Method method
					: sortedMethods
			) {

				// don't add the same method twice

				Pair <String, List <Class <?>>> methodKey =
					Pair.of (
						method.getName (),
						ImmutableList.copyOf (
							method.getParameterTypes ()));

				if (
					contains (
						delegatedMethods,
						methodKey)
				) {
					continue;
				}

				delegatedMethods.add (
					methodKey);

				// write method delegate

				writeDelegationMethod (
					imports,
					formatWriter,
					delegateInterface,
					delegateName,
					method);

			}

		}

	}

	void writeDelegationMethod (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter,
			@NonNull Class <?> delegationInterface,
			@NonNull String delegationName,
			@NonNull Method method) {

		String returnTypeName =
			typeSourceName (
				imports,
				typeParameterMappings,
				method.getGenericReturnType ());

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				Override.class));

		if (
			arrayIsNotEmpty (
				method.getTypeParameters ())
		) {

			formatWriter.writeLineFormat (
				"public <%s>",
				joinWithCommaAndSpace (
					iterableMap (
						Arrays.asList (
							method.getTypeParameters ()),
						typeParameter ->
							typeVariableSourceDeclaration (
								imports,
								typeParameterMappings,
								typeParameter))));

		} else {

			formatWriter.writeLineFormat (
				"public");

		}

		List <Parameter> parameters =
			ImmutableList.copyOf (
				method.getParameters ());

		if (
			collectionIsEmpty (
				parameters)
		) {

			formatWriter.writeLineFormat (
				"%s %s () {",
				returnTypeName,
				method.getName ());

		} else {

			formatWriter.writeLineFormat (
				"%s %s (",
				returnTypeName,
				method.getName ());

			for (
				Parameter parameter
					: listSliceAllButLastItemRequired (
						parameters)
			) {

				formatWriter.writeLineFormat (
					"\t\t%s %s,",
					parameterSourceTypeName (
						imports,
						typeParameterMappings,
						parameter),
					parameter.getName ());

			}

			Parameter lastParameter =
				listLastItemRequired (
					parameters);

			formatWriter.writeLineFormat (
				"\t\t%s %s) {",
				parameterSourceTypeName (
					imports,
					typeParameterMappings,
					lastParameter),
				lastParameter.getName ());

		}

		formatWriter.writeNewline ();

		if (
			stringEqualSafe (
				returnTypeName,
				"void")
		) {

			if (
				collectionIsEmpty (
					parameters)
			) {

				formatWriter.writeLineFormat (
					"\t%s.%s ();",
					delegationName,
					method.getName ());

			} else {

				formatWriter.writeLineFormat (
					"\t%s.%s (",
					delegationName,
					method.getName ());

				for (
					Parameter parameter
						: listSlice (
							parameters,
							0,
							parameters.size () - 1)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						parameter.getName ());

				}

				Parameter lastParameter =
					listLastItemRequired (
						parameters);

				formatWriter.writeLineFormat (
					"\t\t%s);",
					lastParameter.getName ());

			}

		} else {

			if (
				collectionIsEmpty (
					parameters)
			) {

				formatWriter.writeLineFormat (
					"\treturn %s.%s ();",
					delegationName,
					method.getName ());

			} else {

				formatWriter.writeLineFormat (
					"\treturn %s.%s (",
					delegationName,
					method.getName ());

				for (
					Parameter parameter
						: listSlice (
							parameters,
							0,
							parameters.size () - 1)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						parameter.getName ());

				}

				Parameter lastParameter =
					listLastItemRequired (
						parameters);

				formatWriter.writeLineFormat (
					"\t\t%s);",
					lastParameter.getName ());

			}

		}

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	// private implmentation

	private <ComponentAnnotationType extends Annotation>
	String variableNameForDependency (
			@NonNull Class <ComponentAnnotationType> componentAnnotationClass,
			@NonNull Class <?> dependencyClass) {

		ComponentAnnotationType componentAnnotation =
			dependencyClass.getAnnotation (
				componentAnnotationClass);

		return ifThenElse (
			isNotNull (
				componentAnnotation),
			() ->
				genericCastUnchecked (
					methodInvokeByName (
						componentAnnotation,
						"value")),
			() ->
				uncapitalise (
					dependencyClass.getSimpleName ()));

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	public static
	class Dependency {

		Function <JavaImportRegistry, String> classNameSupplier;
		String memberName;
		Class <?> annotationClass;
		Boolean provider;
		Boolean named = false;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class State {

		Function <JavaImportRegistry, String> typeNameSupplier;
		String memberName;

		Boolean getter = false;
		Boolean setter = false;

	}

}
