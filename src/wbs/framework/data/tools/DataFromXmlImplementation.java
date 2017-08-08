package wbs.framework.data.tools;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrElse;
import static wbs.utils.etc.LogicUtils.parseBooleanYesNoEmpty;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.toEnumGeneric;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.NumberUtils.notEqualToOne;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.PropertyUtils.propertySetSimple;
import static wbs.utils.etc.ReflectionUtils.fieldParameterizedType;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataContent;
import wbs.framework.data.annotations.DataElementAttributes;
import wbs.framework.data.annotations.DataElementName;
import wbs.framework.data.annotations.DataIgnore;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.data.annotations.DataSetupMethod;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;
import wbs.utils.io.RuntimeIoException;

/**
 * Automatically builds data objects from XML guided by annotations.
 *
 * TODO separate registry which builds worker
 * TODO read annotations first for efficiency
 */
@PrototypeComponent ("dataFromXmlImplementation")
@Accessors (fluent = true)
public
class DataFromXmlImplementation
	implements DataFromXml {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Map <String, List <DataClassInfo>> dataClassesMap;

	@Getter @Setter
	List <DataClassInfo> elementDataClasses;

	@Getter @Setter
	Map <String, Map <String, ?>> namedObjectCollections;

	// implementation

	@Override
	public
	Optional <Object> readInputStream (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InputStream inputStream,
			@NonNull String filename,
			@NonNull List <Object> parents) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readInputStream");

		) {

			taskLogger.firstErrorFormat (
				"Error reading %s from filesystem",
				filename);

			SAXReader saxReader =
				new SAXReader ();

			Document document;

			try {

				document =
					saxReader.read (
						inputStream,
						filename);

			} catch (DocumentException exception) {

				taskLogger.errorFormatException (
					exception,
					"Error parsing XML");

				return optionalAbsent ();

			}

			Object result;

			try {

				result =
					new ElementBuilder ()

					.element (
						document.getRootElement ())

					.parents (
						parents)

					.context (
						ImmutableList.of (
							document.getRootElement ().getName ()))

					.build (
						taskLogger);

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error building object tree from XML");

				return optionalAbsent ();

			}

			return optionalFromNullable (
				result);

		}

	}

	@Override
	public
	Optional <Object> readClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename,
			@NonNull List <Object> parents) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

		) {

			try (

				InputStream inputStream =
					getClass ().getResourceAsStream (
						filename);

			) {

				if (
					isNull (
						inputStream)
				) {

					taskLogger.errorFormat (
						"Classpath resource %s not found",
						filename);

					return optionalAbsent ();

				}

				return readInputStream (
					taskLogger,
					inputStream,
					filename,
					parents);

			} catch (IOException ioException) {

				taskLogger.errorFormatException (
					ioException,
					"Error reading resource %s",
					filename);

				return optionalAbsent ();

			}

		}

	}

	@Override
	public
	Object readFilenameRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename,
			@NonNull List <Object> parents) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

			InputStream inputStream =
				new FileInputStream (
					filename);

		) {

			return readInputStreamRequired (
				taskLogger,
				inputStream,
				filename,
				parents);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@Accessors (fluent = true)
	class ElementBuilder {

		@Getter @Setter
		Element element;

		@Getter @Setter
		Iterable <Object> parents;

		@Getter @Setter
		Iterable <String> context;

		Object object;

		Set <String> matchedAttributes =
			new HashSet<> ();

		Set <String> matchedElementNames =
			new HashSet<> ();

		Object build (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"ElementBuilder.build");

			) {

				// find builder

				Optional <ComponentProvider <?>> builderOptional =
					findBuilder (
						taskLogger);

				if (
					optionalIsNotPresent (
						builderOptional)
				) {
					return null;
				}

				ComponentProvider <?> builder =
					optionalGetRequired (
						builderOptional);

				// build it

				object =
					builder.provide (
						taskLogger);

				for (
					Field field
						: object.getClass ().getDeclaredFields ()
				) {

					buildField (
						taskLogger,
						field);

				}

				// check for unmatched attributes

				for (
					Object attributeObject
						: element.attributes ()
				) {

					Attribute attribute =
						(Attribute)
						attributeObject;

					String attributeName =
						attribute.getName ();

					if (matchedAttributes.contains (
							attributeName))
						continue;

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't understand attribute %s on <%s>, using %s",
						attributeName,
						element.getName (),
						object.getClass ().getSimpleName ());

				}

				// check for unmatched elements

				long unmatchedElementCount = 0;

				for (
					Object childElementObject
						: element.elements ()
				) {

					Element childElement =
						(Element) childElementObject;

					if (matchedElementNames.contains (
							childElement.getName ()))
						continue;

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't understand element <%s> in <%s>, using %s",
						childElement.getName (),
						element.getName (),
						object.getClass ().getSimpleName ());

					unmatchedElementCount ++;

				}

				if (
					moreThanZero (
						unmatchedElementCount)
				) {
					return null;
				}

				// run init method

				for (
					Method method
						: object.getClass ().getMethods ()
				) {

					DataSetupMethod dataSetupMethodAnnotation =
						method.getAnnotation (
							DataSetupMethod.class);

					if (dataSetupMethodAnnotation == null)
						continue;

					if (
						notEqualToOne (
							method.getParameterTypes ().length)
					) {
						throw new RuntimeException ();
					}

					try {

						methodInvoke (
							method,
							object,
							taskLogger);

					} catch (Exception exception) {

						taskLogger.errorFormatException (
							exception,
							"%s: ",
							joinWithFullStop (
								context),
							"Error invoking data setup method %s.%s",
							object.getClass ().getName (),
							method.getName ());

						return null;

					}

				}

				return object;

			}

		}

		private
		Optional <ComponentProvider <?>> findBuilder (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"findBuilder");

			) {

				Iterator <Object> parentsIterator =
					parents.iterator ();

				Class <?> parentClass =
					parentsIterator.hasNext ()
						? parentsIterator.next ().getClass ()
						: Object.class;

				// find the appropriate builder

				List <DataClassInfo> dataClassInfosForElementName =
					ImmutableList.<DataClassInfo> builder ()

					.addAll (
						mapItemForKeyOrElse (
							dataClassesMap,
							element.getName (),
							() -> emptyList ()))

					.addAll (
						elementDataClasses)

					.build ();

				if (dataClassInfosForElementName == null) {

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't know how to map <%s>",
						element.getName ());

					return optionalAbsent ();

				}

				List <DataClassInfo> matchingDataClassInfos =
					new ArrayList<> ();

				for (
					DataClassInfo dataClassInfo :
						dataClassInfosForElementName
				) {

					if (
						! dataClassInfo.parentClass.isAssignableFrom (
							parentClass)
					) {
						continue;
					}

					matchingDataClassInfos.add (
						dataClassInfo);

				}

				if (matchingDataClassInfos.isEmpty ()) {

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't know how to map <%s> with parent %s",
						element.getName (),
						parentClass.getName ());

					return optionalAbsent ();

				}

				if (matchingDataClassInfos.size () > 1) {

					List <String> matchingDataClassNames =
						new ArrayList<> ();

					for (
						DataClassInfo matchingDataClassInfo
							: matchingDataClassInfos
					) {

						matchingDataClassNames.add (
							matchingDataClassInfo.dataClass ().getName ());

					}

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Multiple mappings for <%s> with parent %s: %s",
						element.getName (),
						parentClass.getName (),
						joinWithCommaAndSpace (
							matchingDataClassNames));

					return optionalAbsent ();

				}

				DataClassInfo dataClassInfo =
					iterableOnlyItemRequired (
						matchingDataClassInfos);

				return optionalOf (
					dataClassInfo.provider);

			}

		}

		void buildField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildField");

			) {

				for (
					Annotation annotation
						: field.getAnnotations ()
				) {

					if (annotation instanceof DataElementName) {

						buildElementNameField (
							taskLogger,
							field,
							(DataElementName) annotation);

					}

					if (annotation instanceof DataElementAttributes) {

						buildElementAttributesField (
							taskLogger,
							field,
							(DataElementAttributes) annotation);

					}

					if (annotation instanceof DataAttribute) {

						buildAttributeField (
							taskLogger,
							field,
							(DataAttribute) annotation);

					}

					if (annotation instanceof DataContent) {

						buildContentField (
							taskLogger,
							field,
							(DataContent) annotation);

					}

					if (annotation instanceof DataChild) {

						buildChildField (
							taskLogger,
							field,
							(DataChild) annotation);

					}

					if (annotation instanceof DataChildren) {

						buildChildrenField (
							taskLogger,
							field,
							(DataChildren) annotation);

					}

					if (annotation instanceof DataChildrenIndex) {

						buildChildrenIndexField (
							field,
							(DataChildrenIndex) annotation);

					}

					if (annotation instanceof DataParent) {

						if (! parents.iterator ().hasNext ())
							throw new RuntimeException ();

						propertySetSimple (
							object,
							field.getName (),
							Object.class,
							optionalOf (
								parents.iterator ().next ()));

					}

					if (annotation instanceof DataAncestor) {

						for (
							Object ancestor
								: parents
						) {

							if (! field.getType ().isInstance (ancestor))
								continue;

							propertySetSimple (
								object,
								field.getName (),
								Object.class,
								optionalOf (
									ancestor));

							break;

						}

					}

					if (annotation instanceof DataIgnore) {

						matchedElementNames.add (
							camelToHyphen (
								field.getName ()));

					}

				}

			}

		}

		void buildAttributeField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataAttribute dataAttributeAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildAttributeField");

			) {

				String attributeName =
					ifNull (
						nullIfEmptyString (
							dataAttributeAnnotation.name ()),
						camelToHyphen (
							field.getName ()));

				String attributeValue =
					element.attributeValue (
						attributeName);

				if (attributeValue == null) {

					if (dataAttributeAnnotation.required ()) {

						taskLogger.errorFormat (
							"%s: ",
							joinWithFullStop (
								context),
							"Missing required attribute '%s' of <%s>",
							attributeName,
							element.getName ());

					}

					return;

				}

				matchedAttributes.add (
					attributeName);

				if (! dataAttributeAnnotation.collection ().isEmpty ()) {

					Map <String, ?> collection =
						namedObjectCollections.get (
							dataAttributeAnnotation.collection ());

					if (collection == null) {

						taskLogger.errorFormat (
							"%s: ",
							joinWithFullStop (
								context),
							"Named collection %s doesn't exist for %s.%s",
							dataAttributeAnnotation.collection (),
							object.getClass ().getSimpleName (),
							field.getName ());

					}

					Object namedObject =
						collection.get (
							attributeValue);

					if (namedObject == null) {

						taskLogger.errorFormat (
							"%s: ",
							joinWithFullStop (
								context),
							"Named object %s not found in collection %s",
							attributeValue,
							dataAttributeAnnotation.collection ());

						return;

					}

					propertySetSimple (
						object,
						field.getName (),
						Object.class,
						optionalOf (
							namedObject));

				} else {

					setScalarFieldRequired (
						taskLogger,
						object,
						field,
						attributeValue);

				}

			}

		}

		void buildElementNameField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataElementName dataElementNameAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildElementNameField");

			) {

				setScalarFieldRequired (
					taskLogger,
					object,
					field,
					element.getName ());

			}

		}

		void buildElementAttributesField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataElementAttributes dataElementNameAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildElementAttributesField");

			) {

				ImmutableMap.Builder <String, String> builder =
					ImmutableMap.builder ();

				for (
					Object attributeObject
						: element.attributes ()
				) {

					Attribute attribute =
						(Attribute)
						attributeObject;

					builder.put (
						attribute.getName (),
						attribute.getValue ());

					matchedAttributes.add (
						attribute.getName ());

				}

				field.setAccessible (
					true);

				fieldSet (
					field,
					object,
					optionalOf (
						builder.build ()));

			}

		}

		boolean tryToSetScalarField (
				@NonNull Object object,
				@NonNull Field field,
				@NonNull String stringValue) {

			if (field.getType () == String.class) {

				propertySetSimple (
					object,
					field.getName (),
					String.class,
					optionalOf (
						stringValue));

				return true;

			} else if (field.getType () == Integer.class) {

				propertySetSimple (
					object,
					field.getName (),
					Integer.class,
					optionalOf (
						toJavaIntegerRequired (
							parseIntegerRequired (
								stringValue))));

				return true;

			} else if (field.getType () == Long.class) {

				propertySetSimple (
					object,
					field.getName (),
					Long.class,
					optionalOf (
						parseIntegerRequired (
							stringValue)));

				return true;

			} else if (field.getType () == Boolean.class) {

				propertySetSimple (
					object,
					field.getName (),
					Boolean.class,
					parseBooleanYesNoEmpty (
						stringValue));

				return true;

			} else if (field.getType () == Optional.class) {

				ParameterizedType parameterizedType =
					fieldParameterizedType (
						field);

				Class <?> optionalClassParam =
					genericCastUnchecked (
						parameterizedType.getActualTypeArguments () [0]);

				if (optionalClassParam == Boolean.class) {

					propertySetSimple (
						object,
						field.getName (),
						Optional.class,
						optionalOf (
							parseBooleanYesNoEmpty (
								stringValue)));

					return true;

				} else {

					return false;

				}

			} else if (field.getType ().isEnum ()) {

				Optional <Enum <?>> enumValueOptional =
					toEnumGeneric (
						field.getType (),
						hyphenToCamel (
							stringValue));

				propertySetSimple (
					object,
					field.getName (),
					Enum.class,
					enumValueOptional);

				return true;

			} else {

				return false;

			}

		}

		void setScalarFieldRequired (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Object object,
				@NonNull Field field,
				@NonNull String stringValue) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"setScalarFieldRequired");

			) {

				if (
					! tryToSetScalarField (
						object,
						field,
						stringValue)
				) {

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't know how to map attribute to %s ",
						field.getType ().getName (),
						"at %s.%s",
						object.getClass ().getSimpleName (),
						field.getName ());

				}

			}

		}

		void buildContentField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataContent dataContentAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildContentField");

			) {

				String stringValue;

				if (
					stringIsNotEmpty (
						dataContentAnnotation.name ())
				) {

					Element contentElement =
						element.element (
							dataContentAnnotation.name ());

					if (
						isNotNull (
							contentElement)
					) {

						matchedElementNames.add (
							dataContentAnnotation.name ());

						stringValue =
							contentElement.getTextTrim ();

					} else if (
						dataContentAnnotation.required ()
					) {

						taskLogger.errorFormat (
							"%s: ",
							joinWithFullStop (
								context),
							"Missing required content element <%s> ",
							dataContentAnnotation.name (),
							"at %s.%s",
							object.getClass ().getSimpleName (),
							field.getName ());

						return;

					} else {

						stringValue = null;

					}

				} else {

					stringValue =
						element.getTextTrim ();

				}

				if (field.getType () == String.class) {

					propertySetSimple (
						object,
						field.getName (),
						String.class,
						optionalOf (
							stringValue));

				} else {

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't know how to map content to %s ",
						field.getType ().getName (),
						"at %s.%s",
						object.getClass ().getSimpleName (),
						field.getName ());

				}

			}

		}

		void buildChildField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataChild dataChildAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildChildField");

			) {

				String childElementName =
					ifNull (
						nullIfEmptyString (
							dataChildAnnotation.name ()),
						camelToHyphen (
							field.getName ()));

				Element childElement =
					element.element (
						childElementName);

				if (
					isNull (
						childElement)
				) {
					return;
				}

				matchedElementNames.add (
					childElementName);

				if (

					collectionIsNotEmpty (
						childElement.attributes ())

					|| collectionIsNotEmpty (
						childElement.elements ())

					|| ! tryToSetScalarField (
						object,
						field,
						childElement.getText ())

				) {

					Object nextParent =
						object;

					Object child =
						new ElementBuilder ()

						.element (
							childElement)

						.parents (
							Iterables.concat (
								Collections.singletonList (
									nextParent),
								parents))

						.context (
							Iterables.concat (
								context,
								Collections.singletonList (
									childElement.getName ())))

						.build (
							taskLogger);

					if (
						isNotNull (
							child)
					) {

						propertySetSimple (
							object,
							field.getName (),
							Object.class,
							optionalOf (
								child));

					}

				}

			}

		}

		void buildChildrenField (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Field field,
				@NonNull DataChildren dataChildrenAnnotation) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"buildChildrenField");

			) {

				if (

					! dataChildrenAnnotation.direct ()

					&& ! dataChildrenAnnotation.childElement ().isEmpty ()

					&& (

						field.getType () != Map.class

						&& field.getType () != List.class

					)

				) {

					taskLogger.errorFormat (
						"%s: ",
						joinWithFullStop (
							context),
						"Don't specify childElement for indirect children, at ",
						"%s.%s",
						field.getDeclaringClass ().getSimpleName (),
						field.getName ());

					return;

				}

				// find the element which contains the children

				Element childrenElement;

				if (dataChildrenAnnotation.direct ()) {

					childrenElement =
						element;

					if (childrenElement == null)
						return;

				} else {

					String childrenElementName =
						! dataChildrenAnnotation.childrenElement ().isEmpty ()
							? dataChildrenAnnotation.childrenElement ()
							: camelToHyphen (
								field.getName ());

					List <?> childrenElementObjects =
						element.elements (
							childrenElementName);

					if (childrenElementObjects.isEmpty ())
						return;

					if (childrenElementObjects.size () > 1)
						throw new RuntimeException ();

					matchedElementNames.add (
						childrenElementName);

					childrenElement =
						(Element)
						childrenElementObjects.get (0);

				}

				// work out parents for recursive call

				Iterable <Object> nextParents;

				if (! dataChildrenAnnotation.surrogateParent ().isEmpty ()) {

					nextParents =
						Iterables.concat (
							ImmutableList.<Object> of (
								PropertyUtils.propertyGetSimple (
									object,
									dataChildrenAnnotation.surrogateParent ()),
								object),
							parents);

				} else {

					nextParents =
						Iterables.concat (
							Collections.singletonList (
								object),
							parents);

				}

				// collect children

				List <Object> children =
					new ArrayList<> ();

				List <?> childElementObjects =
					! dataChildrenAnnotation.childElement ().isEmpty ()
						? childrenElement.elements (
							dataChildrenAnnotation.childElement ())
						: childrenElement.elements ();

				Set <String> newlyMatchedElementNames =
					new HashSet<> ();

				for (
					Object childElementObject
						: childElementObjects
				) {

					Element childElement =
						(Element)
						childElementObject;

					if (dataChildrenAnnotation.direct ()) {

						if (
							contains (
								matchedElementNames,
								childElement.getName ())
						) {
							continue;
						}

						newlyMatchedElementNames.add (
							childElement.getName ());

					}

					if (field.getType () == Map.class) {

						String entryKey =
							childElement.attributeValue (
								dataChildrenAnnotation.keyAttribute ());

						String entryValue =
							childElement.attributeValue (
								dataChildrenAnnotation.valueAttribute ());

						if (entryKey == null) {

							taskLogger.errorFormat (
								"%s: ",
								joinWithFullStop (
									context),
								"Must specify 'keyAttribute' on @DataChildren ",
								"when field type is Map, at %s.%s",
								object.getClass ().getSimpleName (),
								field.getName ());

						}

						if (entryValue == null) {

							taskLogger.errorFormat (
								"%s: ",
								joinWithFullStop (
									context),
								"Must specify 'entryValue' on @DataChildren ",
								"when f	ield type is Map, at %s.%s",
								object.getClass ().getSimpleName (),
								field.getName ());

						}

						if (

							isNull (
								entryKey)

							|| isNull (
								entryValue)

						) {
							return;
						}

						children.add (
							Pair.of (
								entryKey,
								entryValue));

					} else if (
						field.getType () == List.class
						&& ! dataChildrenAnnotation.valueAttribute ().isEmpty ()
					) {

						Type genericType =
							field.getGenericType ();

						Class <?> itemClass;

						if (genericType instanceof ParameterizedType) {

							ParameterizedType parameterizedType =
								(ParameterizedType)
								genericType;

							itemClass =
								(Class <?>)
								parameterizedType.getActualTypeArguments () [0];

						} else {

							itemClass = null;

						}

						String stringValue =
							childElement.attributeValue (
								dataChildrenAnnotation.valueAttribute ());

						if (stringValue == null) {

							taskLogger.errorFormat (
								"%s: ",
								joinWithFullStop (
									context),
								"No attribute '%s' on <%s> ",
								dataChildrenAnnotation.valueAttribute (),
								childElement.getName (),
								"at %s.%s",
								object.getClass ().getSimpleName (),
								field.getName ());

							return;

						}

						Object value;

						if (itemClass == Integer.class) {

							value =
								Integer.parseInt (
									stringValue);

						} else if (itemClass == Long.class) {

							value =
								parseIntegerRequired (
									stringValue);

						} else if (itemClass == String.class) {

							value =
								stringValue;

						} else {

							taskLogger.errorFormat (
								"%s: ",
								joinWithFullStop (
									context),
								"Unable to map attribute type %s ",
								itemClass.getName (),
								"at %s.%s",
								object.getClass ().getSimpleName (),
								field.getName ());

							return;

						}

						children.add (
							value);

					} else {

						children.add (
							new ElementBuilder ()

							.element (
								childElement)

							.parents (
								nextParents)

							.context (
								Iterables.concat (
									context,
									Collections.singletonList (
										childElement.getName ())))

							.build (
								taskLogger)

						);

					}

				}

				matchedElementNames.addAll (
					newlyMatchedElementNames);

				// set them

				if (field.getType () == Map.class) {

					ImmutableMap.Builder<Object,Object> mapBuilder =
						ImmutableMap.builder ();

					for (
						Object pairObject
							: children
					) {

						Pair<?,?> pair =
							(Pair<?,?>) pairObject;

						mapBuilder.put (
							pair.getLeft (),
							pair.getRight());

					}

					propertySetSimple (
						object,
						field.getName (),
						Map.class,
						optionalOf (
							mapBuilder.build ()));

				} else {

					propertySetSimple (
						object,
						field.getName (),
						List.class,
						optionalOf (
							children));

				}

			}

		}

		void buildChildrenIndexField (
				Field field,
				DataChildrenIndex dataChildrenIndexAnnotation) {

			Matcher matcher =
				childrenIndexPattern.matcher (
					field.getName ());

			if (! matcher.matches ())
				throw new RuntimeException ();

			String childrenFieldName =
				matcher.group (1);

			String indexFieldName =
				uncapitalise (
					matcher.group (2));

			Map <Object, Object> childrenIndex =
				new LinkedHashMap<> ();

			List <?> children =
				genericCastUnchecked (
					PropertyUtils.propertyGetSimple (
						object,
						childrenFieldName));

			for (
				Object child
					: children
			) {

				Object index =
					PropertyUtils.propertyGetSimple (
						child,
						indexFieldName);

				if (index == null)
					continue;

				childrenIndex.put (
					index,
					child);

			}

			propertySetSimple (
				object,
				field.getName (),
				Object.class,
				optionalOf (
					childrenIndex));

		}

	}

	static
	Pattern childrenIndexPattern =
		Pattern.compile ("(.+)By(.+)");

	@Accessors (fluent = true)
	@Data
	static
	class DataClassInfo {

		Class <?> parentClass;
		Class <?> dataClass;
		ComponentProvider <?> provider;

	}

}
