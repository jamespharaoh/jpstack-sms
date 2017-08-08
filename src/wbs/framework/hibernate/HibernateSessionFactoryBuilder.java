package wbs.framework.hibernate;

import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.joda.time.Instant;
import org.joda.time.Seconds;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.scaffold.PluginCustomTypeSpec;
import wbs.framework.component.scaffold.PluginEnumTypeSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.helper.SchemaNamesHelperImplementation;
import wbs.framework.sql.SqlLogicImplementation;

@Accessors (fluent = true)
@PrototypeComponent ("hibernateSessionFactoryBuilder")
public
class HibernateSessionFactoryBuilder {

	// singleton dependencies

	@SingletonDependency
	DataSource dataSource;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	@SingletonDependency
	SqlLogicImplementation sqlLogic;

	@SingletonDependency
	SchemaNamesHelperImplementation sqlEntityNames;

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	Properties configProperties;

	// state

	Map <Class <?>, String> customTypes =
		new HashMap <Class <?>, String> ();

	Set <Class <?>> enumTypes =
		new HashSet <Class <?>> ();

	int errorTypes = 0;
	int classErrors = 0;
	int errorClasses = 0;

	// implementation

	void initCustomTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initCustomTypes");

		) {

			for (
				PluginSpec plugin
					: pluginManager.plugins ()
			) {

				if (plugin.models () == null)
					continue;

				for (
					PluginCustomTypeSpec customType
						: plugin.models ().customTypes ()
				) {

					initCustomType (
						taskLogger,
						customType);

				}

				for (
					PluginEnumTypeSpec enumType
						: plugin.models ().enumTypes ()
				) {

					initEnumType (
						taskLogger,
						enumType);

				}

			}

			if (errorTypes > 0) {

				throw new RuntimeException (
					stringFormat (
						"Failed to find %s types",
						integerToDecimalString (
							errorTypes)));

			}

			valueFieldTypes =
				ImmutableSet.<Class<?>>builder ()

				.addAll (
					builtinFieldTypes)

				.addAll (
					customTypes.keySet ())

				.build ();

		}

	}

	void initCustomType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginCustomTypeSpec type) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initCustomType");

		) {

			String objectClassName =
				stringFormat (
					"%s.model.%s",
					type.plugin ().packageName (),
					capitalise (
						type.name ()));

			Optional <Class <?>> objectClassOptional =
				classForName (
					objectClassName);

			if (
				optionalIsNotPresent (
					objectClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					objectClassName);

			}

			String helperClassName =
				stringFormat (
					"%s.hibernate.%sType",
					type.plugin ().packageName (),
					capitalise (
						type.name ()));

			Optional <Class <?>> helperClassOptional =
				classForName (
					helperClassName);

			if (
				optionalIsNotPresent (
					helperClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					helperClassName);

			}

			if (

				optionalIsNotPresent (
					objectClassOptional)

				|| optionalIsNotPresent (
					helperClassOptional)

			) {

				errorTypes ++;

				return;

			}

			customTypes.put (
				objectClassOptional.get (),
				helperClassName);

		}

	}

	void initEnumType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginEnumTypeSpec enumType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initEnumType");

		) {

			String enumClassName =
				stringFormat (
					"%s.model.%s",
					enumType.plugin ().packageName (),
					capitalise (
						enumType.name ()));

			Optional<Class<?>> enumClassOptional =
				classForName (
					enumClassName);

			if (
				optionalIsNotPresent (
					enumClassOptional)
			) {

				taskLogger.errorFormat (
					"Enum class not found: %s",
					enumClassName);

				errorTypes ++;

				return;

			}

			Class<?> enumClass =
				enumClassOptional.get ();

			if (
				contains (
					enumTypes,
					enumClass)
			) {

				throw new RuntimeException (
					stringFormat (
						"Enum class specified multiple times: %s",
						enumClass.getName ()));

			}

			enumTypes.add (
				enumClass);

		}

	}

	public
	SessionFactory build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			initCustomTypes (
				taskLogger);

			Configuration config =
				new Configuration ();

			config.setProperties (
				configProperties);

			SessionFactory sessionFactory =
				buildSessionFactory (
					taskLogger,
					config);

			return sessionFactory;

		}

	}

	SessionFactory buildSessionFactory (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Configuration config) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildSessionFactory");

		) {

			WbsConnectionProvider.setDataSource (
				dataSource);

			config.setProperty (
				"hibernate.connection.provider_class",
				WbsConnectionProvider.class.getName ());

			loadConfiguration (
				taskLogger,
				config);

			taskLogger.noticeFormat (
				"Building session factory");

			Instant startTime =
				Instant.now ();

			ServiceRegistry serviceRegistry =
				new StandardServiceRegistryBuilder ()
					.applySettings (config.getProperties ())
					.build ();

			SessionFactory sessionFactory =
				config.buildSessionFactory (
					serviceRegistry);

			Instant endTime =
				Instant.now ();

			Seconds buildSeconds =
				Seconds.secondsBetween (
					startTime,
					endTime);

			taskLogger.noticeFormat (
				"Session factory built in %s seconds",
				integerToDecimalString (
					buildSeconds.getSeconds ()));

			return sessionFactory;

		}

	}

	void loadConfiguration (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Configuration config) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadConfiguration");

		) {

			taskLogger.noticeFormat (
				"Loading configuration");

			Instant startTime =
				Instant.now ();

			try {

				FileUtils.deleteDirectory (
					new File (
						"work/hibernate"));

				FileUtils.forceMkdir (
					new File (
						"work/hibernate"));

			} catch (IOException exception) {

				taskLogger.errorFormatException (
					exception,
					"Error deleting contents of work/hibernate");

			}

			loadXmlConfigurationReal (
				taskLogger,
				config);

			Instant endTime =
				Instant.now ();

			Seconds buildSeconds =
				Seconds.secondsBetween (
					startTime,
					endTime);

			taskLogger.noticeFormat (
				"Configuration loaded in %s seconds",
				integerToDecimalString (
					buildSeconds.getSeconds ()));

			if (errorClasses > 0) {

				throw new RuntimeException (
					stringFormat (
						"Failed to configure %s entities",
						integerToDecimalString (
							errorClasses)));

			}

		}

	}

	void loadXmlConfigurationReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Configuration config) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadXmlConfigurationReal");

		) {

			for (
				Model <?> model
					: entityHelper.recordModels ()
			) {

				configureModel (
					taskLogger,
					config,
					model);

			}

		}

	}

	public
	void configureModel (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Configuration config,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"configureModel");

		) {

			taskLogger.debugFormat (
				"Loading %s",
				model.objectName ());

			String tableNameSql =
				sqlLogic.quoteIdentifier (
					model.tableName ());

			classErrors = 0;

			// create hibernate xml config for class

			String namespace =
				"http://www.hibernate.org/xsd/hibernate-mapping";

			Document document =
				DocumentHelper.createDocument ();

			Element hibernateMappingElement =
				document

				.addElement (
					"hibernate-mapping",
					namespace)

				.addAttribute (
					QName.get (
						"schemaLocation",
						"xsi",
						"http://www.w3.org/2001/XMLSchema-instance"),
					joinWithSpace (
						"http://www.hibernate.org/xsd/hibernate-mapping",
						"classpath://org/hibernate/hibernate-mapping-4.0.xsd"))

				.addAttribute (
					"package",
					model.objectClass ().getPackage ().getName ());

			Element classElement =
				hibernateMappingElement

				.addElement (
					"class")

				.addAttribute (
					"name",
					model.objectClass ().getSimpleName ())

				.addAttribute (
					"table",
					tableNameSql)

				.addAttribute (
					"lazy",
					"true");

			if (! model.mutable ()) {

				classElement

					.addAttribute (
						"mutable",
						"false");

			}

			// add fields

			for (
				ModelField modelField
					: model.fields ()
			) {

				if (modelField.generatedId ()) {

					configureGeneratedId (
						model,
						modelField,
						classElement);

				} else if (modelField.assignedId ()) {

					configureAssignedId (
						model,
						modelField,
						classElement);

				} else if (modelField.foreignId ()) {

					configureForeignId (
						model,
						modelField,
						classElement);

				} else if (modelField.value ()) {

					configureValue (
						model,
						modelField,
						classElement);

				} else if (modelField.reference ()) {

					configureReference (
						model,
						modelField,
						classElement);

				} else if (modelField.partner ()) {

					configurePartner (
						model,
						modelField,
						classElement);

				} else if (modelField.collection ()) {

					configureCollection (
						taskLogger,
						model,
						modelField,
						classElement);

				} else if (modelField.link ()) {

					configureLink (
						taskLogger,
						model,
						modelField,
						classElement);

				} else if (modelField.compositeId ()) {

					configureCompositeId (
						model,
						modelField,
						classElement);

				} else if (modelField.component ()) {

					configureComponent (
						model,
						modelField,
						classElement);

				} else {

					taskLogger.errorFormat (
						"Don't know how to map %s for %s",
						modelField.type ().name (),
						modelField.fullName ());

					classErrors ++;

				}

			}

			// output document

			File outputFile =
				new File (
					stringFormat (
						"work/hibernate/%s.hbm.xml",
						model.objectClass ().getSimpleName ()));

			OutputFormat format =
				OutputFormat.createPrettyPrint ();

			try (

				OutputStream outputStream =
					new FileOutputStream (
						outputFile);

			) {

				XMLWriter writer =
					new XMLWriter (
						outputStream,
						format);

				writer.write (
					document);

			} catch (IOException exception) {

				taskLogger.warningFormat (
					"Error writing %s",
					outputFile.getAbsolutePath ());

			}

			// skip this class if there were errors

			if (classErrors > 0) {

				taskLogger.errorFormat (
					"Skipping %s due to %s errors",
					model.objectName (),
					integerToDecimalString (
						classErrors));

				errorClasses ++;

				return;

			}

			// add document to hibernate

			config.addCacheableFile (
				outputFile);

		}

	}

	void configureAssignedId (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		String idColumnSql =
			sqlLogic.quoteIdentifier (
				modelField.columnNames ().get (0));

		Element idElement =
			classElement

			.addElement (
				"id")

			.addAttribute (
				"name",
				modelField.name ())

			.addAttribute (
				"column",
				idColumnSql);

		idElement

			.addElement (
				"generator")

			.addAttribute (
				"class",
				"assigned");

	}

	void configureForeignId (
			@NonNull Model <?> model,
			@NonNull ModelField modelField,
			@NonNull Element classElement) {

		String columnSql =
			sqlLogic.quoteIdentifier (
				modelField.columnNames ().get (0));

		Element idElement =
			classElement

			.addElement (
				"id")

			.addAttribute (
				"name",
				"id")

			.addAttribute (
				"column",
				columnSql);

		Element generatorElement =
			idElement

			.addElement (
				"generator")

			.addAttribute (
				"class",
				"foreign");

		generatorElement

			.addElement (
				"param")

			.addAttribute (
				"name",
				"property")

			.addText (
				modelField.foreignFieldName ());

	}

	void configureGeneratedId (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element idElement =
			classElement

			.addElement (
				"id")

			.addAttribute (
				"name",
				"id")

			.addAttribute (
				"column",
				"id");

		String sequenceNameSql =
			sqlLogic.quoteIdentifier (
				modelField.sequenceName ());

		Element generatorElement =
			idElement

			.addElement (
				"generator")

			.addAttribute (
				"class",
				"org.hibernate.id.enhanced.SequenceStyleGenerator");

		generatorElement

			.addElement (
				"param")

			.addAttribute (
				"name",
				"optimizer")

			.addText (
				"none");

		generatorElement

			.addElement (
				"param")

			.addAttribute (
				"name",
				"increment_size")

			.addText (
				"100");

		generatorElement

			.addElement (
				"param")

			.addAttribute (
				"name",
				"sequence_name")

			.addText (
				sequenceNameSql);

	}

	void configureValue (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element propertyElement =
			classElement

			.addElement (
				"property")

			.addAttribute (
				"name",
				modelField.name ());

		// type

		if (
			modelField.hibernateTypeHelper () != null
		) {

			propertyElement

				.addAttribute (
					"type",
					modelField.hibernateTypeHelper ().getName ());

		} else if (
			customTypes.containsKey (
				modelField.valueType ())
		) {

			propertyElement

				.addAttribute (
					"type",
					customTypes.get (
						modelField.valueType ()));


		} else if (
			enumTypes.contains (
				modelField.valueType ())
		) {

			Element typeElement =
				propertyElement

				.addElement (
					"type")

				.addAttribute (
					"name",
					"wbs.framework.hibernate.HibernateEnumType");

			typeElement

				.addElement (
					"param")

				.addAttribute (
					"name",
					"enumClass")

				.addText (
					modelField.valueType ().getName ());

		}

		// column names

		if (modelField.columnNames ().size () == 1) {

			String columnNameSql =
				sqlLogic.quoteIdentifier (
					modelField.columnName ());

			propertyElement

				.addAttribute (
					"column",
					columnNameSql);

		} else {

			for (
				String columnName
					: modelField.columnNames ()
			) {

				String columnNameSql =
					sqlLogic.quoteIdentifier (
						columnName);

				propertyElement

					.addElement (
						"column")

					.addAttribute (
						"name",
						columnNameSql);

			}

		}

	}

	void configureReference (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element manyToOneElement =
			classElement

			.addElement (
				"many-to-one")

			.addAttribute (
				"name",
				modelField.name ());

		String columnName =
			sqlLogic.quoteIdentifier (
				modelField.columnName ());

		manyToOneElement

			.addAttribute (
				"column",
				columnName);

	}

	void configurePartner (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		classElement

			.addElement (
				"one-to-one")

			.addAttribute (
				"name",
				modelField.name ())

			.addAttribute (
				"lazy",
				"proxy");

	}

	void configureCollection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Model <?> model,
			@NonNull ModelField modelField,
			@NonNull Element classElement) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"configureCollection");

		) {

			if (modelField.valueType () == Set.class) {

				configureCollectionSet (
					model,
					modelField,
					classElement);

			} else if (modelField.valueType () == List.class) {

				configureCollectionList (
					taskLogger,
					model,
					modelField,
					classElement);

			} else if (modelField.valueType () == Map.class) {

				configureCollectionMap (
					taskLogger,
					model,
					modelField,
					classElement);

			} else {

				taskLogger.errorFormat (
					"Don't know how to map a collection with type %s for %s",
					modelField.valueType ().getSimpleName (),
					modelField.fullName ());

				classErrors ++;

			}

		}

	}

	void configureCollectionSet (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element setElement =
			classElement

			.addElement (
				"set")

			.addAttribute (
				"name",
				modelField.name ())

			.addAttribute (
				"lazy",
				"true")

			.addAttribute (
				"inverse",
				"true");

		if (modelField.orderSql () != null) {

			setElement

				.addAttribute (
					"order-by",
					modelField.orderSql ());

		}

		if (modelField.whereSql () != null) {

			setElement

				.addAttribute (
					"where",
					modelField.whereSql ());

		}

		// key

		String joinColumnSql =
			sqlLogic.quoteIdentifier (
				ifNull (
					modelField.joinColumnName (),
					sqlEntityNames.idColumnName (
						model.objectClass ())));

		setElement

			.addElement (
				"key")

			.addAttribute (
				"column",
				joinColumnSql);

		// value

		if (modelField.valueColumnName () != null) {

			if (
				doesNotContain (
					valueFieldTypes,
					modelField.collectionValueType ())
			) {
				throw new RuntimeException ();
			}

			String elementColumnSql =
				sqlLogic.quoteIdentifier (
					modelField.valueColumnName ());

			String elementType =
				basicTypes.get (
					modelField.collectionValueType ());

			setElement

				.addElement (
					"element")

				.addAttribute (
					"column",
					elementColumnSql)

				.addAttribute (
					"type",
					elementType);

		} else {

			if (
				contains (
					valueFieldTypes,
					modelField.collectionValueType ())
			) {
				throw new RuntimeException ();
			}

			setElement

				.addElement (
					"one-to-many")

				.addAttribute (
					"class",
					modelField.collectionValueType ().getName ());

		}

	}

	void configureCollectionList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Model <?> model,
			@NonNull ModelField modelField,
			@NonNull Element classElement) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"configureCollectionList");

		) {

			// list

			Element listElement =
				classElement

				.addElement (
					"list")

				.addAttribute (
					"name",
					modelField.name ())

				.addAttribute (
					"lazy",
					"true")

				.addAttribute (
					"inverse",
					"true");

			if (
				isNotNull (
					modelField.orderSql ())
			) {

				listElement

					.addAttribute (
						"order-by",
						modelField.orderSql ());

			}

			if (modelField.whereSql () != null) {

				listElement

					.addAttribute (
						"where",
						modelField.whereSql ());

			}

			// key

			String joinColumnSql =
				sqlLogic.quoteIdentifier (
					ifNull (
						modelField.joinColumnName (),
						sqlEntityNames.idColumnName (
							model.objectClass ())));

			listElement

				.addElement (
					"key")

				.addAttribute (
					"column",
					joinColumnSql);

			// list index

			if (modelField.listIndexColumnName () == null) {

				taskLogger.errorFormat (
					"No index specified for list %s",
					modelField.fullName ());

				classErrors ++;

				return;

			}

			String indexColumnSql =
				sqlLogic.quoteIdentifier (
					modelField.listIndexColumnName ());

			listElement

				.addElement (
					"list-index")

				.addAttribute (
					"column",
					indexColumnSql);

			// value

			if (
				isNotNull (
					modelField.valueColumnName ())
			) {

				if (
					doesNotContain (
						valueFieldTypes,
						modelField.collectionValueType ())
				) {
					throw new RuntimeException ();
				}

				String elementColumnSql =
					sqlLogic.quoteIdentifier (
						modelField.valueColumnName ());

				String elementType =
					basicTypes.get (
						modelField.collectionValueType ());

				listElement

					.addElement (
						"element")

					.addAttribute (
						"column",
						elementColumnSql)

					.addAttribute (
						"type",
						elementType);

			} else {

				if (
					contains (
						valueFieldTypes,
						modelField.collectionValueType ())
				) {
					throw new RuntimeException ();
				}

				listElement

					.addElement (
						"one-to-many")

					.addAttribute (
						"class",
						modelField.collectionValueType ().getName ());

			}

		}

	}

	void configureCollectionMap (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Model <?> model,
			@NonNull ModelField modelField,
			@NonNull Element classElement) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"configureCollectionMap");

		) {

			// map

			Element mapElement =
				classElement

				.addElement (
					"map")

				.addAttribute (
					"name",
					modelField.name ())

				.addAttribute (
					"lazy",
					"true")

				.addAttribute (
					"inverse",
					"true");

			// key

			String keyColumnSql =
				sqlLogic.quoteIdentifier (
					ifNull (
						modelField.joinColumnName (),
						sqlEntityNames.idColumnName (
							model.objectClass ())));

			mapElement

				.addElement (
					"key")

				.addAttribute (
					"column",
					keyColumnSql);

			// map key

			String indexColumnSql =
				sqlLogic.quoteIdentifier (
					modelField.mappingKeyColumnName ());

			String indexType =
				basicTypes.get (
					modelField.collectionKeyType ());

			if (indexType == null) {

				taskLogger.errorFormat (
					"Don't know index type %s for %s",
					modelField.collectionKeyType ().getName (),
					modelField.fullName ());

				classErrors ++;

				return;

			}

			mapElement

				.addElement (
					"map-key")

				.addAttribute (
					"column",
					indexColumnSql)

				.addAttribute (
					"type",
					indexType);

			// value

			if (
				contains (
					valueFieldTypes,
					modelField.collectionValueType ())
			) {
				throw new RuntimeException ();
			}

			mapElement

				.addElement (
					"one-to-many")

				.addAttribute (
					"class",
					modelField.collectionValueType ().getName ());

		}

	}

	void configureLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Model <?> model,
			@NonNull ModelField modelField,
			@NonNull Element classElement) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"configureLink");

		) {

			if (modelField.valueType () == Set.class) {

				ParameterizedType type =
					modelField.parameterizedType ();

				Class<?> referencedClass =
					(Class<?>) type.getActualTypeArguments () [0];

				// set

				Element setElement =
					classElement

					.addElement (
						"set")

					.addAttribute (
						"name",
						modelField.name ())

					.addAttribute (
						"table",
						modelField.associationTableName ())

					.addAttribute (
						"lazy",
						"true");

				if (modelField.owned ()) {

					setElement

						.addAttribute (
							"cascade",
							"all");

				} else {

					setElement

						.addAttribute (
							"inverse",
							"true");

				}

				if (modelField.whereSql () != null) {

					setElement

						.addAttribute (
							"where",
							modelField.whereSql ());

				}

				// key

				String joinColumnSql =
					sqlLogic.quoteIdentifier (
						ifNull (
							modelField.joinColumnName (),
							sqlEntityNames.idColumnName (
								model.objectClass ())));

				setElement

					.addElement (
						"key")

					.addAttribute (
						"column",
						joinColumnSql);

				if (
					isNotNull (
						modelField.valueColumnName ())
				) {

					if (
						doesNotContain (
							valueFieldTypes,
							referencedClass)
					) {

						taskLogger.errorFormat (
							"Invalid element type %s for %s",
							modelField.valueType ().getName (),
							modelField.fullName ());

						return;

					}

					String elementColumnSql =
						sqlLogic.quoteIdentifier (
							modelField.valueColumnName ());

					String elementType =
						basicTypes.get (
							referencedClass);

					setElement

						.addElement (
							"element")

						.addAttribute (
							"column",
							elementColumnSql)

						.addAttribute (
							"type",
							elementType);

				} else {

					if (
						contains (
							valueFieldTypes,
							referencedClass)
					) {
						throw new RuntimeException ();
					}

					String manyToManyColumnSql =
						sqlLogic.quoteIdentifier (
							ifNull (
							modelField.foreignColumnName (),
								sqlEntityNames.idColumnName (
									referencedClass)));

					setElement

						.addElement (
							"many-to-many")

						.addAttribute (
							"column",
							manyToManyColumnSql)

						.addAttribute (
							"class",
							referencedClass.getName ());

				}

			} else if (modelField.valueType () == List.class) {

				ParameterizedType type =
					modelField.parameterizedType ();

				Class<?> referencedClass =
					(Class<?>) type.getActualTypeArguments () [0];

				// list

				Element listElement =
					classElement

					.addElement (
						"list")

					.addAttribute (
						"name",
						modelField.name ())

					.addAttribute (
						"table",
						modelField.associationTableName ())

					.addAttribute (
						"lazy",
						"true");

				if (modelField.owned ()) {

					listElement

						.addAttribute (
							"cascade",
							"all");

				} else {

					listElement

						.addAttribute (
							"inverse",
							"true");

				}

				if (
					isNotNull (
						modelField.whereSql ())
				) {

					listElement

						.addAttribute (
							"where",
							modelField.whereSql ());

				}

				// key

				String keyColumnSql =
					sqlLogic.quoteIdentifier (
						sqlEntityNames.idColumnName (
							model.objectClass ()));

				listElement

					.addElement (
						"key")

					.addAttribute (
						"column",
						keyColumnSql);

				// list index

				if (modelField.listIndexColumnName () != null) {

					String indexColumnSql =
						sqlLogic.quoteIdentifier (
							modelField.listIndexColumnName ());

					listElement

						.addElement (
							"list-index")

						.addAttribute (
							"column",
							indexColumnSql);

				}

				// many to many

				if (valueFieldTypes.contains (referencedClass))
					throw new RuntimeException ();

				String manyToManyColumnSql =
					sqlLogic.quoteIdentifier (
						sqlEntityNames.idColumnName (referencedClass));

				listElement

					.addElement (
						"many-to-many")

					.addAttribute (
						"column",
						manyToManyColumnSql)

					.addAttribute (
						"class",
						referencedClass.getName ());

			} else {

				taskLogger.errorFormat (
					"Don't know how to map link type %s for %s",
					modelField.valueType ().getSimpleName (),
					modelField.fullName ());

				classErrors ++;

			}

		}

	}

	void configureCompositeId (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element compositeIdElement =
			classElement

			.addElement (
				"composite-id")

			.addAttribute (
				"name",
				modelField.name ())

			.addAttribute (
				"class",
				modelField.valueType ().getName ());

		for (
			ModelField compositeIdModelField
				: modelField.fields ()
		) {

			if (compositeIdModelField.reference ()) {

				String columnSql =
					sqlLogic.quoteIdentifier (
						compositeIdModelField.columnName ());

				compositeIdElement

					.addElement (
						"key-many-to-one")

					.addAttribute (
						"name",
						compositeIdModelField.name ())

					.addAttribute (
						"column",
						columnSql);

			} else if (compositeIdModelField.value ()) {

				String columnSql =
					sqlLogic.quoteIdentifier (
						compositeIdModelField.columnName ());

				Element keyPropertyElement =
					compositeIdElement

					.addElement (
						"key-property")

					.addAttribute (
						"name",
						compositeIdModelField.name ())

					.addAttribute (
						"column",
						columnSql);

				String customType =
					customTypes.get (
						compositeIdModelField.valueType ());

				if (customType != null) {

					keyPropertyElement

						.addAttribute (
							"type",
							customType);

				}

			}

		}

	}

	void configureComponent (
			Model <?> model,
			ModelField modelField,
			Element classElement) {

		Element componentElement =
			classElement

			.addElement (
				"component")

			.addAttribute (
				"name",
				modelField.name ())

			.addAttribute (
				"class",
				modelField.valueType ().getName ());

		for (
			ModelField componentModelField
				: modelField.fields ()
		) {

			if (componentModelField.value ()) {

				configureValue (
					model,
					componentModelField,
					componentElement);

			} else if (componentModelField.reference ()) {

				configureReference (
					model,
					componentModelField,
					componentElement);

			}

		}

	}

	Map <Class <?>, String> basicTypes =
		ImmutableMap.<Class <?>, String> builder ()
			.put (String.class, "string")
			.put (Long.class, "long")
			.build ();

	Set <Class <?>> builtinFieldTypes =
		ImmutableSet.<Class <?>> builder ()
			.add (Boolean.class)
			.add (Character.class)
			.add (Date.class)
			.add (Double.class)
			.add (Integer.class)
			.add (String.class)
			.add (new byte [] {}.getClass ())
			.build ();

	Set <Class <?>> valueFieldTypes;

}
