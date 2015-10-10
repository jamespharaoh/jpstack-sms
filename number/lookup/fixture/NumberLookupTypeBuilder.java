package wbs.sms.number.lookup.fixture;

import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.sms.number.lookup.metamodel.NumberLookupTypeSpec;

@PrototypeComponent ("numberLookupTypeBuilder")
@ModelMetaBuilderHandler
public
class NumberLookupTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	NumberLookupTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createNumberLookupType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating number lookup type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					codify (
						spec.name ())),
				exception);

		}

	}

	private
	void createNumberLookupType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextNumberLookupTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('number_lookup_type_id_seq')"));

		ResultSet numberLookupTypeIdResultSet =
			nextNumberLookupTypeIdStatement.executeQuery ();

		numberLookupTypeIdResultSet.next ();

		int numberLookupTypeId =
			numberLookupTypeIdResultSet.getInt (
				1);

		String objectTypeCode =
			ifNull (
				spec.subject (),
				parent.name ());

		Model model =
			entityHelper.modelsByName ().get (
				objectTypeCode);

		if (model == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					objectTypeCode));

		}

		@Cleanup
		PreparedStatement getObjectTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT id ",
					"FROM object_type ",
					"WHERE code = ?"));

		getObjectTypeIdStatement.setString (
			1,
			model.objectTypeCode ());

		ResultSet getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int objectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		@Cleanup
		PreparedStatement insertNumberLookupTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO number_lookup_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertNumberLookupTypeStatement.setInt (
			1,
			numberLookupTypeId);

		insertNumberLookupTypeStatement.setInt (
			2,
			objectTypeId);

		insertNumberLookupTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertNumberLookupTypeStatement.setString (
			4,
			spec.description ());

		insertNumberLookupTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
