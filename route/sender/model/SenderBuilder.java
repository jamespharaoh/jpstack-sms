package wbs.sms.route.sender.model;

import static wbs.framework.utils.etc.Misc.codify;
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
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMetaBuilderHandler;
import wbs.framework.entity.model.ModelMetaSpec;

@PrototypeComponent ("senderBuilder")
@ModelMetaBuilderHandler
public
class SenderBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	SenderSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createSender ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating sender %s",
					codify (
						spec.name ())),
				exception);

		}

	}

	private
	void createSender ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		// allocate sender id

		@Cleanup
		PreparedStatement nextSenderIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('sender_id_seq')"));

		ResultSet senderIdResultSet =
			nextSenderIdStatement.executeQuery ();

		senderIdResultSet.next ();

		int senderId =
			senderIdResultSet.getInt (
				1);

		// create sender

		@Cleanup
		PreparedStatement insertSenderStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO sender (",
						"id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?)"));

		insertSenderStatement.setInt (
			1,
			senderId);

		insertSenderStatement.setString (
			2,
			codify (
				spec.name ()));

		insertSenderStatement.setString (
			3,
			spec.description ());

		insertSenderStatement.executeUpdate ();

		// commit transaction

		connection.commit ();

	}

}
