package wbs.framework.schema.tool;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.schema.model.Schema;
import wbs.framework.schema.model.SchemaColumn;
import wbs.framework.schema.model.SchemaForeignKey;
import wbs.framework.schema.model.SchemaIndex;
import wbs.framework.schema.model.SchemaPrimaryKey;
import wbs.framework.schema.model.SchemaSequence;
import wbs.framework.schema.model.SchemaTable;
import wbs.framework.sql.SqlLogic;

@SingletonComponent ("schemaToSql")
public
class SchemaToSqlImplementation
	implements SchemaToSql {

	// singleton implementation

	@SingletonDependency
	SqlLogic sqlLogic;

	// constants

	public final static
	String statementIndent = "";

	public final static
	String statementSeparator = " ";

	public final static
	String columnIndent = "";

	public final static
	String columnSeparator = ", ";

	@Override
	public
	void forSchema (
			@NonNull List <String> sqlStatements,
			@NonNull Schema schema) {

		forEnumTypes (
			sqlStatements,
			schema.enumTypes ());

		forSchemaTables (
			sqlStatements,
			new ArrayList<> (
				schema.tables ().values ()));

	}

	@Override
	public
	void forEnumTypes (
			@NonNull List <String> sqlStatements,
			@NonNull Map <String, List <String>> enumTypes) {

		for (
			Map.Entry <String, List <String>> entry
				: enumTypes.entrySet ()
		) {

			doEnumType (
				sqlStatements,
				entry.getKey (),
				entry.getValue ());

		}

	}

	@Override
	public
	void forSchemaTables (
			@NonNull List <String> sqlStatements,
			@NonNull List <SchemaTable> schemaTables) {

		for (
			SchemaTable schemaTable
				: schemaTables
		) {

			doTableFirstPass (
				sqlStatements,
				schemaTable);

		}

		for (
			SchemaTable schemaTable
				: schemaTables
		) {

			doTableSecondPass (
				sqlStatements,
				schemaTable);

		}

	}

	public
	void doEnumType (
			@NonNull List<String> sqlStatements,
			@NonNull String name,
			@NonNull List<String> values) {

		List<String> parts =
			new ArrayList<String> ();

		parts.add (
			stringFormat (
				"CREATE TYPE %s",
				sqlLogic.quoteIdentifier (name)));

		parts.add (
			stringFormat (
				"AS ENUM ("));

		List<String> valuesSql =
			new ArrayList<String> ();

		for (String value : values) {

			valuesSql.add (
				sqlLogic.quoteString (value));

		}

		parts.add (
			joinColumns (valuesSql));

		parts.add (
			stringFormat (
				")"));

		sqlStatements.add (
			joinStatements (parts));

	}

	public
	void doTableFirstPass (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable) {

		sqlStatements.add (
			stringFormat (
				"CREATE TABLE %s ()",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		for (
			SchemaSequence schemaSequence
				: schemaTable.sequences ().values ()
		) {

			doSequence (
				sqlStatements,
				schemaTable,
				schemaSequence);

		}

		for (
			SchemaColumn schemaColumn
				: schemaTable.columns ().values ()
		) {

			doColumn (
				sqlStatements,
				schemaTable,
				schemaColumn);

		}

		if (schemaTable.primaryKey () != null) {

			doPrimaryKey (
				sqlStatements,
				schemaTable,
				schemaTable.primaryKey ());

		}

	}

	public
	void doTableSecondPass (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable) {

		for (
			SchemaForeignKey schemaForeignKey
				: schemaTable.foreignKeys ()
		) {

			doForeignKey (
				sqlStatements,
				schemaTable,
				schemaForeignKey);

		}

		for (
			SchemaIndex schemaIndex
				: schemaTable.indexes ().values ()
		) {

			doIndex (
				sqlStatements,
				schemaTable,
				schemaIndex);

		}

	}

	public
	void doSequence (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable,
			@NonNull SchemaSequence schemaSequence) {

		List<String> parts =
			new ArrayList<String> ();

		parts.add (
			stringFormat (
				"CREATE SEQUENCE %s",
				sqlLogic.quoteIdentifier (schemaSequence.name ())));

		sqlStatements.add (
			joinStatements (parts));

	}

	public
	void doColumn (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable,
			@NonNull SchemaColumn schemaColumn) {

		List <String> parts =
			new ArrayList<> ();

		parts.add (
			stringFormat (
				"ALTER TABLE %s",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		parts.add (
			stringFormat (
				"ADD COLUMN %s %s",
				sqlLogic.quoteIdentifier (
					schemaColumn.name ()),
				schemaColumn.type ()));

		if (! schemaColumn.nullable ()) {

			parts.add (
				stringFormat (
					"NOT NULL"));

		}

		sqlStatements.add (
			joinStatements (parts));

	}

	public
	void doPrimaryKey (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable,
			@NonNull SchemaPrimaryKey schemaPrimaryKey) {

		String primaryKeyName =
			stringFormat (
				"%s_pk",
				schemaTable.name ());

		// create unique index

		List<String> indexParts =
			new ArrayList<String> ();

		indexParts.add (
			stringFormat (
				"CREATE UNIQUE INDEX %s",
				sqlLogic.quoteIdentifier (
					primaryKeyName)));

		indexParts.add (
			stringFormat (
				"ON %s (",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		List<String> columnParts =
			new ArrayList<String> ();

		for (
			String column
				: schemaPrimaryKey.columns ()
		) {

			columnParts.add (
				sqlLogic.quoteIdentifier (
					column));

		}

		indexParts.add (
			joinColumns (
				columnParts));

		indexParts.add (
			stringFormat (
				")"));

		sqlStatements.add (
			joinStatements (
				indexParts));

		// create primary key constraint

		List<String> constraintParts =
			new ArrayList<String> ();

		constraintParts.add (
			stringFormat (
				"ALTER TABLE %s",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		constraintParts.add (
			stringFormat (
				"ADD PRIMARY KEY USING INDEX %s",
				sqlLogic.quoteIdentifier (
					primaryKeyName)));

		sqlStatements.add (
			joinStatements (
				constraintParts));

	}

	public
	void doForeignKey (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable,
			@NonNull SchemaForeignKey schemaForeignKey) {

		List<String> constraintParts =
			new ArrayList<String> ();

		constraintParts.add (
			stringFormat (
				"ALTER TABLE %s",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		constraintParts.add (
			stringFormat (
				"ADD FOREIGN KEY ("));

		List<String> columnParts =
			new ArrayList<String> ();

		for (
			String column
				: schemaForeignKey.sourceColumns ()
		) {

			columnParts.add (
				sqlLogic.quoteIdentifier (
					column));

		}

		constraintParts.add (
			joinStatements (
				columnParts));

		constraintParts.add (
			stringFormat (
				")"));

		constraintParts.add (
			stringFormat (
				"REFERENCES %s",
				sqlLogic.quoteIdentifier (
					schemaForeignKey.targetTable ())));

		sqlStatements.add (
			joinStatements (
				constraintParts));

	}

	public
	void doIndex (
			@NonNull List<String> sqlStatements,
			@NonNull SchemaTable schemaTable,
			@NonNull SchemaIndex schemaIndex) {

		List<String> createIndexParts =
			new ArrayList<String> ();

		if (schemaIndex.unique ()) {

			createIndexParts.add (
				stringFormat (
					"CREATE UNIQUE INDEX %s",
					sqlLogic.quoteIdentifier (
						schemaIndex.name ())));

		} else {

			createIndexParts.add (
				stringFormat (
					"CREATE INDEX %s",
					sqlLogic.quoteIdentifier (
						schemaIndex.name ())));

		}


		createIndexParts.add (
			stringFormat (
				"ON %s (",
				sqlLogic.quoteIdentifier (
					schemaTable.name ())));

		createIndexParts.add (
			joinColumns (
				schemaIndex.columns ()));

		createIndexParts.add (
			stringFormat (
				")"));

		sqlStatements.add (
			joinStatements (
				createIndexParts));

	}

	public
	String joinParts (
			@NonNull List<String> parts,
			@NonNull String indent,
			@NonNull String separator) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (
			String part
				: parts
		) {

			if (stringBuilder.length () > 0)
				stringBuilder.append (separator);

			stringBuilder.append (indent);
			stringBuilder.append (part);

		}

		return stringBuilder.toString ();

	}

	public
	String joinStatements (
			@NonNull List<String> parts) {

		return joinParts (
			parts,
			statementIndent,
			statementSeparator);

	}

	public
	String joinColumns (
			@NonNull List<String> parts) {

		return joinParts (
			parts,
			columnIndent,
			columnSeparator);

	}

}
