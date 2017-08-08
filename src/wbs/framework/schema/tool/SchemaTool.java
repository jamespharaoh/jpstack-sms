package wbs.framework.schema.tool;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.LoggingDataSource;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.builder.SchemaFromModel;
import wbs.framework.schema.helper.SchemaTypesHelper;
import wbs.framework.schema.model.Schema;

import wbs.utils.string.AtomicFileWriter;

public
class SchemaTool {

	// singleton dependencies

	@SingletonDependency
	CachedViewToSql cachedViewToSql;

	@SingletonDependency
	LoggingDataSource dataSource;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SchemaToSql schemaToSql;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SchemaFromModel> schemaFromModelProvider;

	// state

	Schema schema;
	List <String> sqlStatements;

	// implementation

	public
	void schemaCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"schemaCreate");

		) {

			defineTables (
				taskLogger);

			writeSchemaSqlScript (
				taskLogger);

			executeSchemaSqlScript (
				taskLogger);

			createObjectTypes (
				taskLogger);

		}

	}

	private
	void defineTables (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"defineTables");

		) {

			schema =
				schemaFromModelProvider.provide (
					taskLogger)

				.enumTypes (
					schemaTypesHelper.enumTypes ())

				.modelsByClass (
					entityHelper.recordModelsByClass ())

				.build (
					taskLogger)

			;

			taskLogger.makeException ();

		}

	}

	private
	void writeSchemaSqlScript (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeSchemaSqlScript");

		) {

			sqlStatements =
				new ArrayList <String> ();

			schemaToSql.forSchema (
				sqlStatements,
				schema);

			for (
				Model <?> model
					: entityHelper.recordModels ()
			) {

				if (
					isNull (
						model.cachedView ())
				) {
					continue;
				}

				cachedViewToSql.forModel (
					taskLogger,
					sqlStatements,
					model);

			}

			// write sql

			try (

				AtomicFileWriter fileWriter =
					new AtomicFileWriter (
						"work/schema.sql");

			) {

				for (
					String sqlStatement
						: sqlStatements
				) {

					fileWriter.writeString (
						sqlStatement);

					fileWriter.writeString (
						";\n");

				}

				fileWriter.close ();

			}

		}

	}

	void executeSchemaSqlScript (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"executeSchemaSqlScript");

			Connection connection =
				dataSource.getConnection (
					taskLogger);

		) {

		// execute sql

			connection.setAutoCommit (
				false);

			int errors = 0;

			try (

				Statement statement =
					connection.createStatement ();

			) {

				taskLogger.noticeFormat (
					"Running schema create script");

				for (
					String sqlStatement
						: sqlStatements
				) {

					try {

						statement.execute (
							sqlStatement);

					} catch (Exception exception) {

						if (errors == 0) {

							taskLogger.errorFormatException (
								exception,
								"Error: %s",
								sqlStatement);

						} else if (errors < 100) {

							taskLogger.errorFormat (
								"Error: %s",
								sqlStatement);

						}

						errors ++;

					}

				}

			}

			if (errors > 100) {

				taskLogger.errorFormat (
					"Additional %s errors not shown",
					integerToDecimalString (
						errors - 100));

			}

			if (errors > 0) {

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						integerToDecimalString (
							errors)));

			}

			connection.commit ();

			taskLogger.noticeFormat (
				"Schema created successfully");

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

	void createObjectTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createObjectTypes");

			Connection connection =
				dataSource.getConnection (
					taskLogger);

		) {

			connection.setAutoCommit (
				false);

			try (

				PreparedStatement nextObjectTypeIdStatement =
					connection.prepareStatement (
						stringFormat (
							"SELECT ",
								"nextval ('object_type_id_seq')"));

				PreparedStatement insertObjectTypeStatement =
					connection.prepareStatement (
						stringFormat (
							"INSERT INTO object_type (",
								"id, ",
								"code) ",
							"VALUES (",
								"?, ",
								"?)"));

			) {

				taskLogger.noticeFormat (
					"Creating object types");

				for (
					Model <?> model
						: entityHelper.recordModels ()
				) {

					int objectTypeId;

					if (
						stringEqualSafe (
							model.objectTypeCode (),
							"root")
					) {

						objectTypeId = 0;

					} else {

						try (

							ResultSet objectTypeIdResultSet =
								nextObjectTypeIdStatement.executeQuery ();

						) {

							objectTypeIdResultSet.next ();

							objectTypeId =
								objectTypeIdResultSet.getInt (
									1);

						}

					}

					insertObjectTypeStatement.setInt (
						1,
						objectTypeId);

					insertObjectTypeStatement.setString (
						2,
						model.objectTypeCode ());

					insertObjectTypeStatement.executeUpdate ();

				}

				connection.commit ();

				taskLogger.noticeFormat (
					"Object types created successfully");

			}

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

}
