package wbs.utils.etc;

import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.reflect.MethodUtils;

import wbs.utils.exception.RuntimeIllegalAccessException;
import wbs.utils.exception.RuntimeInstantiationException;
import wbs.utils.exception.RuntimeInvocationTargetException;
import wbs.utils.exception.RuntimeNoSuchMethodException;

public
class ReflectionUtils {

	public static
	Object fieldGet (
			@NonNull Field field,
			@NonNull Object object) {

		try {

			return field.get (
				object);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		}

	}

	public static
	void fieldSet (
			@NonNull Field field,
			@NonNull Object object,
			@NonNull Optional <Object> value) {

		try {

			field.set (
				object,
				optionalOrNull (
					value));

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		}

	}

	public static <Type>
	Constructor <Type> getConstructor (
			@NonNull Class <Type> theClass,
			@NonNull Class <?> ... parameterTypes) {

		try {

			return theClass.getConstructor (
				parameterTypes);

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		}

	}

	public static <Type>
	Type constructorInvoke (
			@NonNull Constructor <Type> constructor,
			@NonNull Object ... parameters) {

		try {

			return constructor.newInstance (
				parameters);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		} catch (InstantiationException instantiationException) {

			throw new RuntimeInstantiationException (
				instantiationException);

		}

	}

	public static
	Optional <Method> methodGet (
			@NonNull Class <?> objectClass,
			@NonNull String name,
			@NonNull List <Class <?>> parameterTypes) {

		try {

			return optionalOf (
				objectClass.getMethod (
					name,
					parameterTypes.toArray (
						new Class<?> [0])));

		} catch (NoSuchMethodException noSuchMethodException) {

			return optionalAbsent ();

		}

	}

	public static
	Method methodGetRequired (
			@NonNull Class <?> objectClass,
			@NonNull String name,
			@NonNull List <Class <?>> parameterTypes) {

		try {

			return objectClass.getMethod (
				name,
				parameterTypes.toArray (
					new Class<?> [0]));

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Method methodGetDeclaredRequired (
			@NonNull Class <?> objectClass,
			@NonNull String name,
			@NonNull List <Class <?>> parameterTypes) {

		try {

			return objectClass.getDeclaredMethod (
				name,
				parameterTypes.toArray (
					new Class <?> [0]));

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		}

	}

	public static
	Method methodGetStaticRequired (
			@NonNull Class <?> containingClass,
			@NonNull String methodName,
			@NonNull List <Class <?>> parameterTypes) {

		Method method =
			methodGetDeclaredRequired (
				containingClass,
				methodName,
				parameterTypes);

		if (
			methodIsNotStatic (
				method)
		) {

			throw new RuntimeNoSuchMethodException (
				"Method is not static");

		}

		return method;

	}

	public static
	Method methodGetByNameRequired (
			@NonNull Class <?> theClass,
			@NonNull String methodName) {

		List <Method> methods =
			Arrays.stream (
				theClass.getMethods ())

			.filter (
				method ->
					stringEqualSafe (
						methodName,
						method.getName ()))

			.collect (
				Collectors.toList ());

		if (
			collectionIsEmpty (
				methods)
		) {

			throw new RuntimeNoSuchMethodException (
				stringFormat (
					"No such method: %s.%s",
					classNameFull (
						theClass),
					methodName));

		}

		if (
			collectionHasMoreThanOneElement (
				methods)
		) {

			throw new RuntimeException (
				stringFormat (
					"Found %s methods: %s.%s",
					integerToDecimalString (
						collectionSize (
							methods)),
					classNameFull (
						theClass),
					methodName));

		}

		return listFirstElementRequired (
			methods);

	}

	public static
	boolean methodIsNotStatic (
			@NonNull Method method) {

		return ! Modifier.isStatic (
			method.getModifiers ());

	}

	public static
	Object methodInvoke (
			@NonNull Method method,
			@NonNull Object object,
			@NonNull Object ... parameters) {

		try {

			return method.invoke (
				object,
				parameters);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		}

	}

	public static
	Object methodInvokeByName (
			@NonNull Object object,
			@NonNull String methodName,
			@NonNull Object ... parameters) {

		try {

			return MethodUtils.invokeMethod (
				object,
				methodName,
				parameters);

		} catch (NoSuchMethodException noSuchMethodException) {

			throw new RuntimeNoSuchMethodException (
				noSuchMethodException);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeIllegalAccessException (
				illegalAccessException);

		} catch (InvocationTargetException invocationTargetException) {

			throw new RuntimeInvocationTargetException (
				invocationTargetException);

		}

	}

	public static
	ParameterizedType fieldParameterizedType (
			@NonNull Field field) {

		return genericCastUnchecked (
			field.getGenericType ());

	}

}
