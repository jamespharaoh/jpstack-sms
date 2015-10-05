package wbs.sms.message.delivery.model;

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

@PrototypeComponent ("deliveryTypeBuilder")
@ModelMetaBuilderHandler
public
class DeliveryTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	DeliveryTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createDeliveryType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating delivery type %s",
					codify (
						spec.name ())),
				exception);

		}

	}

	private
	void createDeliveryType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextDeliveryTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('delivery_type_id_seq')"));

		ResultSet deliveryTypeIdResultSet =
			nextDeliveryTypeIdStatement.executeQuery ();

		deliveryTypeIdResultSet.next ();

		int deliveryTypeId =
			deliveryTypeIdResultSet.getInt (
				1);

		@Cleanup
		PreparedStatement insertDeliveryTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO delivery_type (",
						"id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?)"));

		insertDeliveryTypeStatement.setInt (
			1,
			deliveryTypeId);

		insertDeliveryTypeStatement.setString (
			2,
			codify (
				spec.name ()));

		insertDeliveryTypeStatement.setString (
			3,
			spec.description ());

		insertDeliveryTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
