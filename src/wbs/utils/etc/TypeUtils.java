package wbs.utils.etc;

import static wbs.utils.collection.ArrayUtils.arrayIsEmpty;
import static wbs.utils.collection.ArrayUtils.arrayMap;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrKey;
import static wbs.utils.etc.LogicUtils.referenceNotEqualUnsafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.codegen.JavaImportRegistry;

import wbs.utils.exception.RuntimeIllegalAccessException;
import wbs.utils.exception.RuntimeInstantiationException;
import wbs.utils.exception.RuntimeInvocationTargetException;
import wbs.utils.exception.RuntimeNoSuchMethodException;
import wbs.utils.exception.RuntimeSecurityException;

public
class TypeUtils {

	// ---------- class instances

	public static
	boolean isInstanceOf (
			@NonNull Class <?> theClass,
			@NonNull Object object) {

		return theClass.isInstance (
			object);

	}

	public static
	boolean isNotInstanceOf (
			@NonNull Class <?> theClass,
			@NonNull Object object) {

		return ! theClass.isInstance (
			object);

	}

	public static <CastType>
	Optional <CastType> dynamicCast (
			@NonNull Class <CastType> classToCastTo,
			@NonNull Object value) {

		if (
			classToCastTo.isInstance (
				value)
		) {

			return optionalOf (
				classToCastTo.cast (
					value));

		} else {

			return optionalAbsent ();

		}

	}

	public static <CastType>
	CastType dynamicCastRequired (
			@NonNull Class <CastType> classToCastTo,
			@NonNull Object value) {

		return classToCastTo.cast (
			value);

	}

	public static
	boolean isSubclassOf (
			@NonNull Class <?> superclass,
			@NonNull Class <?> subclass) {

		return superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isNotSubclassOf (
			@NonNull Class <?> superclass,
			@NonNull Class <?> subclass) {

		return ! superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isSuperclassOf (
			@NonNull Class <?> subclass,
			@NonNull Class <?> superclass) {

		return superclass.isAssignableFrom (
			subclass);

	}

	public static
	boolean isNotSuperclassOf (
			@NonNull Class <?> subclass,
			@NonNull Class <?> superclass) {

		return ! superclass.isAssignableFrom (
			subclass);

	}

	// ---------- class equality

	public static
	boolean classEqualSafe (
			@NonNull Class <?> class0,
			@NonNull Class <?> class1) {

		return class0 == class1;

	}

	public static
	boolean classNotEqual (
			@NonNull Class <?> class0,
			@NonNull Class <?> class1) {

		return class0 != class1;

	}

	public static
	boolean classInSafe (
			@NonNull Class <?> value,
			@NonNull Iterable <Class <?>> examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean classInSafe (
			@NonNull Class <?> value,
			@NonNull Class <?> ... examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean classNotInSafe (
			@NonNull Class <?> value,
			@NonNull Class <?> ... examples) {

		for (
			Class <?> example
				: examples
		) {

			if (value == example) {
				return false;
			}

		}

		return true;

	}

	// ---------- class lookup

	public static
	Optional <Class <?>> classForName (
			@NonNull String className) {

		try {

			return Optional.<Class<?>>of (
				Class.forName (
					className));

		} catch (ClassNotFoundException exception) {

			return Optional.absent ();

		}

	}

	public static
	Optional <Class <?>> classForName (
			@NonNull String packageName,
			@NonNull String className) {

		try {

			return Optional.<Class<?>>of (
				Class.forName (
					packageName + "." + className));

		} catch (ClassNotFoundException exception) {

			return Optional.absent ();

		}

	}

	public static
	Class<?> classForNameRequired (
			@NonNull String className) {

		try {

			return Class.forName (
				className);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Class<?> classForNameRequired (
			@NonNull String packageName,
			@NonNull String className) {

		try {

			return Class.forName (
				packageName + "." + className);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Optional <Class <?>> classForNameFormat (
			@NonNull String ... arguments) {

		return classForName (
			stringFormatArray (
				arguments));

	}

	public static
	Class <?> classForNameFormatRequired (
			@NonNull String ... arguments) {

		return classForNameRequired (
			stringFormatArray (
				arguments));

	}

	public static
	Class <?> classForNameOrThrow (
			@NonNull String className,
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		return optionalOrThrow (
			classForName (className),
			exceptionSupplier);

	}

	// ---------- class names

	public static
	String classNameSimple (
			@NonNull Class <?> theClass) {

		return theClass.getSimpleName ();

	}

	public static
	String objectClassNameSimple (
			@NonNull Object object) {

		return object.getClass ().getSimpleName ();

	}

	public static
	String classNameFull (
			@NonNull Class <?> theClass) {

		return theClass.getName ();

	}

	public static
	String objectClassNameFull (
			@NonNull Object object) {

		return object.getClass ().getName ();

	}

	public static
	String classPackageName (
			@NonNull Class <?> theClass) {

		return theClass.getPackage ().getName ();

	}

	public static
	String objectPackageName (
			@NonNull Object object) {

		return object.getClass ().getPackage ().getName ();

	}

	public static
	String typeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull Type type) {

		if (
			isInstanceOf (
				Class.class,
				type)
		) {

			return classSourceName (
				imports,
				(Class <?>)
				type);

		} else if (
			isInstanceOf (
				ParameterizedType.class,
				type)
		) {

			return parameterizedTypeSourceName (
				imports,
				typeParameterMappings,
				(ParameterizedType)
				type);

		} else if (
			isInstanceOf (
				TypeVariable.class,
				type)
		) {

			return typeVariableSourceName (
				imports,
				typeParameterMappings,
				(TypeVariable <?>)
				type);

		} else if (
			isInstanceOf (
				WildcardType.class,
				type)
		) {

			return wildcardTypeSourceName (
				imports,
				(WildcardType)
				type);

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle a %s",
					type.getClass ().getSimpleName ()));

		}

	}

	public static
	String classSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Class <?> theClass) {

		if (theClass.isArray ()) {

			return stringFormat (
				"%s[]",
				classSourceName (
					imports,
					theClass.getComponentType ()));

		} else {

			return imports.register (
				theClass);

		}

	}

	public static
	String parameterizedTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull ParameterizedType parameterizedType) {

		if (
			arrayIsEmpty (
				parameterizedType.getActualTypeArguments ())
		) {

			return imports.register (
				(Class <?>)
				parameterizedType.getRawType ());

		} else {

			return stringFormat (
				"%s <%s>",
				typeSourceName (
					imports,
					typeParameterMappings,
					parameterizedType.getRawType ()),
				joinWithCommaAndSpace (
					arrayMap (
						typeArgument ->
							typeSourceName (
								imports,
								typeParameterMappings,
								typeArgument),
						parameterizedType.getActualTypeArguments ())));

		}

	}

	public static
	String typeVariableSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull TypeVariable <?> typeVariable) {

		return mapItemForKeyOrKey (
			typeParameterMappings,
			typeVariable.getName ());

	}

	public static
	String typeVariableSourceDeclaration (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull TypeVariable <?> typeVariable) {

		List <Type> bounds =
			Arrays.stream (
				typeVariable.getBounds ())

			.filter (
				bound ->
					referenceNotEqualUnsafe (
						bound,
						Object.class))

			.collect (
				Collectors.toList ());

		if (
			collectionIsEmpty (
				bounds)
		) {

			return typeVariable.getName ();

		} else {

			return stringFormat (
				"%s extends %s",
				typeVariable.getName (),
				joinWithCommaAndSpace (
					iterableMap (
						bounds,
						bound ->
							typeSourceName (
								imports,
								typeParameterMappings,
								bound))));

		}

	}

	public static
	String wildcardTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull WildcardType wildcardType) {

		return wildcardType.toString ();

	}

	public static
	String parameterSourceTypeName (
			@NonNull JavaImportRegistry imports,
			@NonNull Map <String, String> typeParameterMappings,
			@NonNull Parameter parameter) {

		if (parameter.isVarArgs ()) {

			return stringFormat (
				"%s ...",
				typeSourceName (
					imports,
					typeParameterMappings,
					parameter.getType ().getComponentType ()));

		} else {

			return typeSourceName (
				imports,
				typeParameterMappings,
				parameter.getParameterizedType ());

		}

	}

	public static <ClassType>
	ClassType classInstantiate (
			@NonNull Class <ClassType> classToInstantiate) {

		try {

			return classToInstantiate.newInstance ();

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InstantiationException instantiationException) {

			throw new RuntimeInstantiationException (
				instantiationException);

		}

	}

	public static <ClassType>
	ClassType classInstantiate (
			@NonNull Class <ClassType> classToInstantiate,
			@NonNull List <Class <?>> constructorParameterTypes,
			@NonNull List <Object> constructorParameters) {

		try {

			Constructor <ClassType> constructor =
				classToInstantiate.getConstructor (
					constructorParameterTypes.toArray (
						new Class <?> [] {}));

			return constructor.newInstance (
				constructorParameters.toArray ());

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		} catch (SecurityException securityException) {

			throw new RuntimeSecurityException (
				securityException);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InstantiationException instantiationException) {

			throw new RuntimeInstantiationException (
				instantiationException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		}

	}

	public static
	Class <?> rawType (
			@NonNull Type type) {

		if (
			isInstanceOf (
				Class.class,
				type)
		) {

			return genericCastUnchecked (
				type);

		} else if (
			isInstanceOf (
				ParameterizedType.class,
				type)
		) {

			ParameterizedType parameterizedType =
				genericCastUnchecked (
					type);

			return genericCastUnchecked (
				parameterizedType.getRawType ());

		} else {

			throw new ClassCastException ();

		}

	}

	@SuppressWarnings ("unchecked")
	public static <ToClass>
	ToClass genericCastUnchecked (
			@NonNull Object object) {

		return (ToClass)
			object;

	}

	@SuppressWarnings ("unchecked")
	public static <ToClass>
	ToClass genericCastUncheckedNullSafe (
			Object object) {

		return (ToClass)
			object;

	}

	public static
	List <Type> classAllGenericInterfaces (
			@NonNull Class <?> subjectClass) {

		ImmutableSet.Builder <Type> interfacesBuilder =
			ImmutableSet.builder ();

		Optional <Class <?>> currentClassOptional =
			optionalOf (
				subjectClass);

		while (
			optionalIsPresent (
				currentClassOptional)
		) {

			Class <?> currentClass =
				optionalGetRequired (
					currentClassOptional);

			interfacesBuilder.addAll (
				Arrays.asList (
					currentClass.getGenericInterfaces ()));

			currentClassOptional =
				optionalFromNullable (
					currentClass.getSuperclass ());

		}

		return ImmutableList.copyOf (
			interfacesBuilder.build ());

	}

}
