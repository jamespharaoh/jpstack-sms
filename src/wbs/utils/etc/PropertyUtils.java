package wbs.utils.etc;

import static wbs.utils.collection.ArrayUtils.arrayFirstItemRequired;
import static wbs.utils.collection.CollectionUtils.arrayLength;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.notEqualToOne;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.utils.exception.RuntimeIllegalAccessException;
import wbs.utils.exception.RuntimeInvocationTargetException;
import wbs.utils.exception.RuntimeNoSuchMethodException;

public
class PropertyUtils {

	private
	PropertyUtils () {
		// never instantiated
	}

	public static
	Object propertyGetAuto (
			@NonNull Object object,
			@NonNull String propertyName) {

		try {

			Class <?> objectClass =
				object.getClass ();

			Method getter =
				propertyGetMethodAuto (
					objectClass,
					propertyName);

			return getter.invoke (
				object);

		} catch (IllegalAccessException exception) {

			throw new RuntimeIllegalAccessException (
				exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeInvocationTargetException (
				exception);

		}

	}

	public static
	Method propertyGetMethodAuto (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		try {

			return objectClass.getMethod (
				propertyGetterName (propertyName));

		} catch (NoSuchMethodException exception) {
		}

		try {

			return objectClass.getMethod (
				propertyIsGetterName (propertyName));

		} catch (NoSuchMethodException exception) {
		}

		try {

			return objectClass.getMethod (
				propertyName);

		} catch (NoSuchMethodException exception) {
		}

		throw new RuntimeNoSuchMethodException (
			stringFormat (
				"Can't find getter for %s on %s",
				propertyName,
				objectClass.getName ()));

	}

	public static
	Object propertyGetSimple (
			@NonNull Object object,
			@NonNull String propertyName) {

		try {

			Class <?> objectClass =
				object.getClass ();

			Method getter =
				objectClass.getMethod (
					propertyName);

			return getter.invoke (
				object);

		} catch (NoSuchMethodException exception) {

			throw new RuntimeNoSuchMethodException (
				exception);

		} catch (IllegalAccessException exception) {

			throw new RuntimeIllegalAccessException (
				exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeInvocationTargetException (
				exception);

		}

	}

	public static
	void propertySetAuto (
			@NonNull Object object,
			@NonNull String propertyName,
			Object newValue) {

		try {

			Class <?> objectClass =
				object.getClass ();

			Method setter =
				propertySetMethodAuto (
					objectClass,
					propertyName);

			Class <?> propertyClass =
				setter.getParameterTypes () [0];

			if (
				isNotNull (
					newValue)
			) {

				Class <?> valueClass =
					newValue.getClass ();

				if (
					isNotSubclassOf (
						propertyClass,
						valueClass)
				) {

					throw new ClassCastException (
						stringFormat (
							"Cannot set property %s.%s ",
							classNameFull (
								objectClass),
							propertyName,
							"of type %s ",
							classNameFull (
								propertyClass),
							"to %s",
							classNameFull (
								valueClass)));


				}

			}

			setter.invoke (
				object,
				newValue);

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	Method propertySetMethodAuto (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		String setterName =
			propertySetterName (propertyName);

		Method[] methods =
			objectClass.getMethods ();

		for (Method method
				: methods) {

			if (method.getParameterTypes ().length != 1)
				continue;

			if (! method.getName ().equals (setterName))
				continue;

			return method;

		}

		for (
			Method method
				: methods
		) {

			if (method.getParameterTypes ().length != 1)
				continue;

			if (! method.getName ().equals (propertyName))
				continue;

			return method;

		}

		throw new RuntimeNoSuchMethodException (
			stringFormat (
				"Can't find setter for %s on %s",
				propertyName,
				objectClass.getName ()));

	}

	public static
	void propertySetSimple (
			@NonNull Object object,
			@NonNull String propertyName,
			@NonNull Class <?> propertyClass,
			@NonNull Optional <?> newValueOpional) {

		try {

			Class <?> objectClass =
				object.getClass ();

			Method[] methods =
				objectClass.getMethods ();

			if (
				optionalIsPresent (
					newValueOpional)

				&& isNotInstanceOf (
					propertyClass,
					optionalGetRequired (
						newValueOpional))

			) {

				Object newValue =
					optionalGetRequired (
						newValueOpional);

				throw new ClassCastException (
					stringFormat (
						"New value class %s ",
						classNameSimple (
							newValue.getClass ()),
						"is not instance of %s",
						classNameSimple (
							propertyClass)));

			}

			for (
				Method method
					: methods
			) {

				if (
					stringNotEqualSafe (
						method.getName (),
						propertyName)
				) {
					continue;
				}

				if (
					notEqualToOne (
						arrayLength (
							method.getParameterTypes ()))
				) {
					continue;
				}

				Class <?> parameterClass =
					arrayFirstItemRequired (
						method.getParameterTypes ());

				if (

					isNotSubclassOf (
						parameterClass,
						propertyClass)

					&& isNotSubclassOf (
						propertyClass,
						parameterClass)

				) {
					continue;
				}

				if (

					optionalIsPresent (
						newValueOpional)

					&& isNotInstanceOf (
						parameterClass,
						optionalGetRequired (
							newValueOpional))

				) {

					Object newValue =
						optionalGetRequired (
							newValueOpional);

					throw new ClassCastException (
						stringFormat (
							"Setter %s.%s (%s) ",
							classNameFull (
								objectClass),
							propertyName,
							classNameSimple (
								parameterClass),
							"can't be set to %s",
							classNameSimple (
								newValue.getClass ())));

				}

				method.invoke (
					object,
					optionalOrNull (
						newValueOpional));

				return;

			}

			throw new RuntimeNoSuchMethodException (
				stringFormat (
					"Method %s.%s (%s) not found",
					objectClass.getName (),
					propertyName,
					classNameSimple (
						propertyClass)));

		} catch (IllegalAccessException exception) {

			throw new RuntimeIllegalAccessException (
				exception);

		} catch (InvocationTargetException exception) {

			throw new RuntimeInvocationTargetException (
				exception);

		}

	}

	public static
	String propertyGetterName (
			String name) {

		return "get"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String propertyIsGetterName (
			String name) {

		return "is"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String propertySetterName (
			String name) {

		return "set"
			+ Character.toUpperCase (name.charAt (0))
			+ name.substring (1);

	}

	public static
	String nameFromGetter (
			String getterName) {

		Matcher matcher =
			getterPattern.matcher (
				getterName);

		if (! matcher.matches ())
			throw new IllegalArgumentException ();

		String upcasedName =
			matcher.group (1);

		return Character.toLowerCase (upcasedName.charAt (0))
				+ upcasedName.substring (1);

	}

	public static
	String nameFromSetter (
			String setterName) {

		Matcher matcher =
			setterPattern.matcher (setterName);

		if (! matcher.matches ())
			throw new IllegalArgumentException ();

		String upcasedName =
			matcher.group (1);

		return Character.toLowerCase (upcasedName.charAt (0))
				+ upcasedName.substring (1);

	}

	private final static
	Pattern getterPattern =
		Pattern.compile ("get([A-Z].*)");

	private final static
	Pattern setterPattern =
		Pattern .compile ("set([A-Z].*)");

	public static
	boolean isSetter (
			String name) {

		return setterPattern
			.matcher (name)
			.matches ();

	}

	public static
	Class<?> propertyClassForObject (
			@NonNull Object object,
			@NonNull String propertyName) {

		Class<?> objectClass =
			object.getClass ();

		return propertyClassForClass (
			objectClass,
			propertyName);

	}

	public static
	Class<?> propertyClassForClass (
			@NonNull Class<?> objectClass,
			@NonNull String propertyName) {

		Method getter =
			propertyGetMethodAuto (
				objectClass,
				propertyName);

		return getter.getReturnType ();

	}

	public static
	void checkRequiredProperties (
			@NonNull List <RequiredProperty> requiredProperties,
			@NonNull Object object) {

		for (
			RequiredProperty requiredProperty
				: requiredProperties
		) {

			Object propertyValue =
				propertyGetAuto (
					object,
					requiredProperty.name ());

			switch (requiredProperty.type ()) {

			case notNull:

				if (
					isNull (
						propertyValue)
				) {

					throw new IllegalStateException (
						stringFormat (
							"Property '%s' of '%s' must not be null",
							requiredProperty.name (),
							object.getClass ().getSimpleName ()));

				}

				break;

			case notEmptyCollection:

				if (

					isNull (
						propertyValue)

					|| collectionIsEmpty (
						(Collection <?>)
						propertyValue)

				) {

					throw new IllegalStateException (
						stringFormat (
							"Property '%s' of '%s' ",
							requiredProperty.name (),
							object.getClass ().getSimpleName (),
							"must not be empty or null"));

				}

				break;

			case notEmptyString:

				if (

					isNull (
						propertyValue)

					|| stringIsEmpty (
						stringTrim (
							(String)
							propertyValue))

				) {

					throw new IllegalStateException (
						stringFormat (
							"Property '%s' of '%s' ",
							requiredProperty.name (),
							object.getClass ().getSimpleName (),
							"must not be empty or null"));

				}

				break;

			}

		}

	}

	@Accessors (fluent = true)
	@Data
	public static
	class RequiredProperty {

		String name;
		RequiredPropertyType type;

		public static
		RequiredProperty notNull (
				@NonNull String name) {

			return new RequiredProperty ()

				.name (
					name)

				.type (
					RequiredPropertyType.notNull);

		}

		public static
		RequiredProperty notEmptyCollection (
				@NonNull String name) {

			return new RequiredProperty ()

				.name (
					name)

				.type (
					RequiredPropertyType.notEmptyCollection);

		}

		public static
		RequiredProperty notEmptyString (
				@NonNull String name) {

			return new RequiredProperty ()

				.name (
					name)

				.type (
					RequiredPropertyType.notEmptyString);

		}

	}

	public static
	enum RequiredPropertyType {
		notNull,
		notEmptyCollection,
		notEmptyString;
	}

}
