package wbs.framework.schema.tool;

import static wbs.utils.collection.CollectionUtils.collectionAddAll;
import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoItems;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapWithIndex;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.FormatWriterUtils.writeLines;
import static wbs.utils.string.FormatWriterUtils.writeLinesWithCommaAlways;
import static wbs.utils.string.FormatWriterUtils.writeLinesWithCommaExceptLastLine;
import static wbs.utils.string.StringUtils.joinWithSeparator;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.cachedview.CachedAggregateFieldSpec;
import wbs.framework.entity.meta.cachedview.CachedGroupFieldSpec;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.sql.SqlLogic;

import wbs.utils.string.LazyFormatWriter;

@SingletonComponent ("cachedViewToSql")
public
class CachedViewToSqlImplementation
	implements CachedViewToSql {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SqlLogic sqlLogic;

	// public implementation

	@Override
	public
	void forModel (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"forModel",
					keyEqualsString (
						"objectName",
						model.objectName ()));

		) {

			createLogTable (
				taskLogger,
				sqlStatements,
				model);

		}

	}

	// private implementation

	private
	void createLogTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createLogTable");

		) {

			CachedViewSpec cachedView =
				model.cachedView ();

			// collect column data

			List <String> columnDefinitions =
				new ArrayList<> ();

			columnDefinitions.add (
				stringFormat (
					"id bigint PRIMARY KEY DEFAULT nextval (%s)",
					sqlLogic.quoteStringFormat (
						"%s_updates_pending_id_seq",
						model.tableName ())));

			List <IndexColumn> indexColumns =
				new ArrayList<> ();

			List <ValueColumn> valueColumns =
				new ArrayList<> ();

			for (
				CachedGroupFieldSpec groupField
					: cachedView.groupFields ()
			) {

				String fieldName =
					ifNull (
						groupField.fieldName (),
						groupField.name ());

				List <String> fieldNameParts =
					stringSplitFullStop (
						fieldName);

				if (
					collectionHasOneItem (
						fieldNameParts)
				) {

					throw todo ();

				} else if (
					collectionHasTwoItems (
						fieldNameParts)
				) {

					ModelField compositeField =
						model.field (
							listFirstElementRequired (
								fieldNameParts));

					Model <?> compositeModel =
						mapItemForKeyRequired (
							entityHelper.compositeModelsByClass (),
							compositeField.valueType ());

					ModelField actualField =
						compositeModel.field (
							listSecondElementRequired (
								fieldNameParts));

					for (
						long index = 0;
						index < collectionSize (
							actualField.columnNames ());
						index ++
					) {

						String columnName =
							listItemAtIndexRequired (
								actualField.columnNames (),
								index);

						String columnSqlType =
							listItemAtIndexRequired (
								actualField.columnSqlTypes (),
								index);

						columnDefinitions.add (
							stringFormat (
								"%s %s NOT NULL",
								sqlLogic.quoteIdentifier (
									columnName),
								columnSqlType));

						indexColumns.add (
							new IndexColumn ()

							.sqlColumnName (
								sqlLogic.quoteIdentifier (
									columnName))

							.sqlColumnType (
								columnSqlType)

						);

					}

				} else {

					throw todo ();

				}

			}

			for (
				CachedAggregateFieldSpec aggregateField
					: cachedView.aggregateFields ()
			) {

				String fieldName =
					ifNull (
						aggregateField.fieldName (),
						aggregateField.name ());

				List <String> fieldNameParts =
					stringSplitFullStop (
						fieldName);

				if (
					collectionHasOneItem (
						fieldNameParts)
				) {

					throw todo ();

				} else if (
					collectionHasTwoItems (
						fieldNameParts)
				) {

					ModelField compositeField =
						model.field (
							listFirstElementRequired (
								fieldNameParts));

					Model <?> compositeModel =
						mapItemForKeyRequired (
							entityHelper.compositeModelsByClass (),
							compositeField.valueType ());

					ModelField actualField =
						compositeModel.field (
							listSecondElementRequired (
								fieldNameParts));

					for (
						long index = 0;
						index < collectionSize (
							actualField.columnNames ());
						index ++
					) {

						String columnName =
							listItemAtIndexRequired (
								actualField.columnNames (),
								index);

						String columnSqlType =
							listItemAtIndexRequired (
								actualField.columnSqlTypes (),
								index);

						columnDefinitions.add (
							stringFormat (
								"%s %s NOT NULL",
								sqlLogic.quoteIdentifier (
									columnName),
								columnSqlType));

						valueColumns.add (
							new ValueColumn ()

							.sqlColumnName (
								sqlLogic.quoteIdentifier (
									columnName))

							.sqlColumnType (
								columnSqlType)

							.sqlWhen (
								aggregateField.when ())

						);

					}

				} else {

					throw todo ();

				}

			}

			// create sql

			createSequence (
				taskLogger,
				sqlStatements,
				model);

			createTable (
				taskLogger,
				sqlStatements,
				model,
				columnDefinitions);

			createUpdatesPendingInsertFunction (
				taskLogger,
				sqlStatements,
				model,
				indexColumns,
				valueColumns);

			createUpdateFunction (
				taskLogger,
				sqlStatements,
				model,
				indexColumns,
				valueColumns);

			createProcessFunction (
				taskLogger,
				sqlStatements,
				model,
				indexColumns,
				valueColumns);

			createRecalculateOneFunction (
				taskLogger,
				sqlStatements,
				model,
				indexColumns,
				valueColumns);

			createRecalculateRangeFunction (
				taskLogger,
				sqlStatements,
				model,
				indexColumns,
				valueColumns);

			createUpdatesPendingInsertTriggers (
				taskLogger,
				sqlStatements,
				model);

		}

	}

	private
	void createSequence (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createSequence");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			formatWriter.writeLineFormat (
				"CREATE SEQUENCE %s;",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending_id_seq",
					model.tableName ()));

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <String> columnDefinitions) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createTable");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"CREATE TABLE %s (",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			writeLines (
				formatWriter,
				"%s",
				"%s,",
				"%s,",
				"%s",
				columnDefinitions);

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createUpdatesPendingInsertFunction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <IndexColumn> indexColumns,
			@NonNull List <ValueColumn> valueColumns) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createUpdatesPendingInsertFunction");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			formatWriter.writeLineFormat (
				"CREATE OR REPLACE FUNCTION %s ()",
				stringFormat (
					"%s_updates_pending_insert",
					model.tableName ()));

			formatWriter.writeLineFormat (
				"RETURNS trigger AS $$");

			formatWriter.writeLineFormatIncreaseIndent (
				"BEGIN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"IF TG_OP IN ('UPDATE', 'DELETE') THEN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"INSERT INTO %s VALUES (",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			List <String> minusValues =
				new ArrayList<> ();

			minusValues.add (
				stringFormat (
					"nextval (%s)",
					sqlLogic.quoteStringFormat (
						"%s_updates_pending_id_seq",
						model.tableName ())));

			collectionAddAll (
				minusValues,
				iterableMap (
					indexColumns,
					indexColumn ->
						stringFormat (
							"OLD.%s",
							indexColumn.sqlColumnName)));

			collectionAddAll (
				minusValues,
				iterableMap (
					valueColumns,
					valueColumn ->
						stringFormat (
							"CASE WHEN %s THEN -1 ELSE 0 END",
							stringReplaceAllSimple (
								"$",
								"OLD",
								valueColumn.sqlWhen ()))));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				minusValues);

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"END IF;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"IF TG_OP IN ('INSERT', 'UPDATE') THEN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"INSERT INTO %s VALUES (",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			List <String> plusValues =
				new ArrayList<> ();

			plusValues.add (
				stringFormat (
					"nextval (%s)",
					sqlLogic.quoteStringFormat (
						"%s_updates_pending_id_seq",
						model.tableName ())));

			collectionAddAll (
				plusValues,
				iterableMap (
					indexColumns,
					indexColumn ->
						stringFormat (
							"NEW.%s",
							indexColumn.sqlColumnName)));

			collectionAddAll (
				plusValues,
				iterableMap (
					valueColumns,
					valueColumn ->
						stringFormat (
							"CASE WHEN %s THEN 1 ELSE 0 END",
							stringReplaceAllSimple (
								"$",
								"NEW",
								valueColumn.sqlWhen ()))));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				plusValues);

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"END IF;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"RETURN NULL;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"END;");

			formatWriter.writeLineFormatDecreaseIndent (
				"$$ LANGUAGE 'plpgsql';");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createUpdatesPendingInsertTriggers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createUpdatesPendingInsertTriggers");

		) {

			CachedViewSpec cachedView =
				model.cachedView ();

			Model <?> sourceModel =
				mapItemForKeyRequired (
					entityHelper.recordModelsByName (),
					cachedView.sourceObjectName ());

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_insert_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER INSERT ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending_insert",
						model.tableName ())));

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_update_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER UPDATE ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending_insert",
						model.tableName ())));

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_delete_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER DELETE ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending_insert",
						model.tableName ())));

		}

	}

	private
	void createUpdateFunction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <IndexColumn> indexColumns,
			@NonNull List <ValueColumn> valueColumns) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createUpdateFunction");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			// open function declaration

			formatWriter.writeLineFormatIncreaseIndent (
				"CREATE OR REPLACE FUNCTION %s (",
				stringFormat (
					"%s_update",
					model.tableName ()));

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnType));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					valueColumns,
					ValueColumn::sqlColumnType));

			formatWriter.writeLineFormatDecreaseIndent (
				") RETURNS void AS $$");

			formatWriter.writeLineFormatIncreaseIndent (
				"BEGIN");

			formatWriter.writeNewline ();

			// attempt update

			formatWriter.writeLineFormat (
				"UPDATE %s",
				sqlLogic.quoteIdentifier (
					model.tableName ()));

			formatWriter.writeLineFormatIncreaseIndent (
				"SET");

			formatWriter.writeNewline ();

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMapWithIndex (
					valueColumns,
					(columnIndex, valueColumn) ->
						stringFormat (
							"%s = %s + $%s",
							valueColumn.sqlColumnName (),
							valueColumn.sqlColumnName (),
							integerToDecimalString (
								sum (
									collectionSize (
										indexColumns),
									columnIndex,
									1l)))));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				"WHERE");

			formatWriter.writeNewline ();

			writeLines (
				formatWriter,
				"%s",
				"%s",
				"AND %s",
				"AND %s",
				iterableMapWithIndex (
					indexColumns,
					(columnIndex, indexColumn) ->
						stringFormat (
							"%s = $%s",
							indexColumn.sqlColumnName (),
							integerToDecimalString (
								columnIndex + 1))));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				";");

			formatWriter.writeNewline ();

			// insert if update failed

			formatWriter.writeLineFormatIncreaseIndent (
				"IF NOT FOUND THEN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"INSERT INTO %s (",
				sqlLogic.quoteIdentifier (
					model.tableName ()));

			formatWriter.writeLineFormat (
				"id,");

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnName));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					valueColumns,
					ValueColumn::sqlColumnName));

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				") VALUES (");

			formatWriter.writeLineFormat (
				"nextval (%s),",
				sqlLogic.quoteStringFormat (
					"%s_id_seq",
					model.tableName ()));

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMapWithIndex (
					indexColumns,
					(columnIndex, indexColumn) ->
						stringFormat (
							"$%s",
							integerToDecimalString (
								columnIndex + 1))));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMapWithIndex (
					valueColumns,
					(columnIndex, valueColumn) ->
						stringFormat (
							"$%s",
							integerToDecimalString (
								sum (
									collectionSize (
										indexColumns),
									columnIndex,
									1l)))));

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"END IF;");

			formatWriter.writeNewline ();

			// close function declaration

			formatWriter.writeLineFormatDecreaseIndent (
				"END;");

			formatWriter.writeLineFormatDecreaseIndent (
				"$$ LANGUAGE 'plpgsql';");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createProcessFunction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <IndexColumn> indexColumns,
			@NonNull List <ValueColumn> valueColumns) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createProcessFunction");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			// open function declaration

			formatWriter.writeLineFormat (
				"CREATE OR REPLACE FUNCTION %s ()",
				stringFormat (
					"%s_updates_pending_process",
					model.tableName ()));

			formatWriter.writeLineFormat (
				"RETURNS text AS $$");

			// declare variables

			formatWriter.writeLineFormatIncreaseIndent (
				"DECLARE");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"row RECORD;");

			formatWriter.writeLineFormat (
				"update_counter int;");

			formatWriter.writeLineFormat (
				"row_counter int;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				"BEGIN");

			formatWriter.writeNewline ();

			// set row counter

			formatWriter.writeLineFormatIncreaseIndent (
				"row_counter := (");

			formatWriter.writeLineFormat (
				"SELECT count (*)");

			formatWriter.writeLineFormat (
				"FROM %s",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			formatWriter.writeNewline ();

			// initialise update counter

			formatWriter.writeLineFormat (
				"update_counter := 0;");

			formatWriter.writeNewline ();

			// iterate grouped rows

			formatWriter.writeLineFormatIncreaseIndent (
				"FOR row IN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"SELECT");

			formatWriter.writeNewline ();

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnName));

			formatWriter.writeNewline ();

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					valueColumns,
					valueColumn ->
						stringFormat (
							"sum (%s)::%s AS %s",
							valueColumn.sqlColumnName (),
							valueColumn.sqlColumnType (),
							valueColumn.sqlColumnName ())));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"FROM %s",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"GROUP BY");

			formatWriter.writeNewline ();

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnName));

			formatWriter.writeNewline ();

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				"LOOP");

			formatWriter.writeNewline ();

			// perform update

			formatWriter.writeLineFormatIncreaseIndent (
				"PERFORM %s (",
				sqlLogic.quoteIdentifierFormat (
					"%s_update",
					model.tableName ()));

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMap (
					indexColumns,
					indexColumn ->
						stringFormat (
							"row.%s",
							indexColumn.sqlColumnName ())));

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					valueColumns,
					valueColumn ->
						stringFormat (
							"row.%s",
							valueColumn.sqlColumnName ())));

			formatWriter.writeLineFormatDecreaseIndent (
				");");

			formatWriter.writeNewline ();

			// close loop

			formatWriter.writeLineFormatDecreaseIndent (
				"END LOOP;");

			formatWriter.writeNewline ();

			// delete updates pending

			formatWriter.writeLineFormat (
				"DELETE FROM %s;",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			formatWriter.writeNewline ();

			// return

			formatWriter.writeLineFormat (
				"RETURN %s;",
				joinWithSeparator (
					" || ",
					sqlLogic.quoteString (
						"Processed "),
					sqlLogic.quoteIdentifier (
						"row_counter"),
					sqlLogic.quoteString (
						" rows in "),
					sqlLogic.quoteIdentifier (
						"update_counter"),
					sqlLogic.quoteString (
						" updates")));

			formatWriter.writeNewline ();

			// close function declaration

			formatWriter.writeLineFormatDecreaseIndent (
				"END;");

			formatWriter.writeLineFormatDecreaseIndent (
				"$$ LANGUAGE 'plpgsql';");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createRecalculateOneFunction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <IndexColumn> indexColumns,
			@NonNull List <ValueColumn> valueColumns) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createRecalculateOneFunction");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			CachedViewSpec cachedView =
				model.cachedView ();

			Model <?> sourceModel =
				mapItemForKeyRequired (
					entityHelper.recordModelsByName (),
					cachedView.sourceObjectName ());

			// open function declaration

			IndexColumn firstIndexColumn =
				listFirstElementRequired (
					indexColumns);

			formatWriter.writeLineFormat (
				"CREATE OR REPLACE FUNCTION %s (%s)",
				stringFormat (
					"%s_recalculate",
					model.tableName ()),
				firstIndexColumn.sqlColumnType ());

			formatWriter.writeLineFormat (
				"RETURNS text AS $$");

			formatWriter.writeLineFormatIncreaseIndent (
				"BEGIN");

			formatWriter.writeNewline ();

			// delete from target

			formatWriter.writeLineFormat (
				"DELETE FROM %s",
				sqlLogic.quoteIdentifier (
					model.tableName ()));

			formatWriter.writeLineFormat (
				"WHERE %s = $1;",
				firstIndexColumn.sqlColumnName ());

			formatWriter.writeNewline ();

			// delete from updates_pending

			formatWriter.writeLineFormat (
				"DELETE FROM %s",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			formatWriter.writeLineFormat (
				"WHERE %s = $1;",
				firstIndexColumn.sqlColumnName ());

			formatWriter.writeNewline ();

			// insert new values

			formatWriter.writeLineFormat (
				"INSERT INTO %s",
				sqlLogic.quoteIdentifier (
					model.tableName ()));

			formatWriter.writeLineFormatIncreaseIndent (
				"SELECT");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"nextval (%s),",
				sqlLogic.quoteStringFormat (
					"%s_id_seq",
					model.tableName ()));

			formatWriter.writeNewline ();

			writeLinesWithCommaAlways (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnName));

			formatWriter.writeNewline ();

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					valueColumns,
					valueColumn ->
						stringFormat (
							"sum (%s)::%s",
							stringFormat (
								"CASE WHEN %s THEN 1 ELSE 0 END",
								stringReplaceAllSimple (
									"$.",
									"",
									valueColumn.sqlWhen ())),
							valueColumn.sqlColumnType ())));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"FROM %s",
				sqlLogic.quoteIdentifier (
					sourceModel.tableName ()));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"WHERE %s = $1",
				firstIndexColumn.sqlColumnName ());

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"GROUP BY");

			formatWriter.writeNewline ();

			writeLinesWithCommaExceptLastLine (
				formatWriter,
				iterableMap (
					indexColumns,
					IndexColumn::sqlColumnName));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				";");

			formatWriter.writeNewline ();

			// return

			formatWriter.writeLineFormat (
				"RETURN 'TODO - useful information here';");

			// close function declaration

			formatWriter.writeLineFormatDecreaseIndent (
				"END;");

			formatWriter.writeLineFormatDecreaseIndent (
				"$$ LANGUAGE 'plpgsql';");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	private
	void createRecalculateRangeFunction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model,
			@NonNull List <IndexColumn> indexColumns,
			@NonNull List <ValueColumn> valueColumns) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createRecalculateRangeFunction");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ("  ");

		) {

			// open function declaration

			IndexColumn firstIndexColumn =
				listFirstElementRequired (
					indexColumns);

			formatWriter.writeLineFormat (
				"CREATE OR REPLACE FUNCTION %s (%s, %s)",
				stringFormat (
					"%s_recalculate",
					model.tableName ()),
				firstIndexColumn.sqlColumnType (),
				firstIndexColumn.sqlColumnType ());

			formatWriter.writeLineFormat (
				"RETURNS text AS $$");

			formatWriter.writeLineFormatIncreaseIndent (
				"DECLARE");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"current_value %s;",
				firstIndexColumn.sqlColumnType ());

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				"BEGIN");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"current_value := $1;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"WHILE current_value < $2 LOOP");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"PERFORM %s (current_value);",
				sqlLogic.quoteIdentifierFormat (
					"%s_recalculate",
					model.tableName ()));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"END LOOP;");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"RETURN 'TODO - useful information here';");

			formatWriter.writeNewline ();

			// close function declaration

			formatWriter.writeLineFormatDecreaseIndent (
				"END;");

			formatWriter.writeLineFormatDecreaseIndent (
				"$$ LANGUAGE 'plpgsql';");

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	private static
	class IndexColumn {
		String sqlColumnName;
		String sqlColumnType;
	}

	@Accessors (fluent = true)
	@Data
	private static
	class ValueColumn {
		String sqlColumnName;
		String sqlColumnType;
		String sqlWhen;
	}

}
