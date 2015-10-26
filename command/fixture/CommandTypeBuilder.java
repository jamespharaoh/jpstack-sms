package wbs.sms.command.fixture;

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
import lombok.extern.log4j.Log4j;

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
import wbs.sms.command.metamodel.CommandTypeSpec;

@Log4j
@PrototypeComponent ("commandTypeBuilder")
@ModelMetaBuilderHandler
public
class CommandTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	CommandTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			log.info (
				stringFormat (
					"Create command type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					codify (
						spec.name ())));

			createCommandType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating command type %s.%s",
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
	void createCommandType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextCommandTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('command_type_id_seq')"));

		ResultSet commandTypeIdResultSet =
			nextCommandTypeIdStatement.executeQuery ();

		commandTypeIdResultSet.next ();

		int commandTypeId =
			commandTypeIdResultSet.getInt (
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
		PreparedStatement insertCommandTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO command_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description, ",
						"deleted) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"false)"));

		insertCommandTypeStatement.setInt (
			1,
			commandTypeId);

		insertCommandTypeStatement.setInt (
			2,
			objectTypeId);

		insertCommandTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertCommandTypeStatement.setString (
			4,
			spec.description ());

		insertCommandTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
