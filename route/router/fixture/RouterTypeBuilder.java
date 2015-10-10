package wbs.sms.route.router.fixture;

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
import wbs.sms.route.router.metamodel.RouterTypeSpec;

@PrototypeComponent ("routerTypeBuilder")
@ModelMetaBuilderHandler
public
class RouterTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	RouterTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createRouterType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating router type %s.%s",
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
	void createRouterType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextRouterTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('router_type_id_seq')"));

		ResultSet routerTypeIdResultSet =
			nextRouterTypeIdStatement.executeQuery ();

		routerTypeIdResultSet.next ();

		int routerTypeId =
			routerTypeIdResultSet.getInt (
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
		PreparedStatement insertRouterTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO router_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertRouterTypeStatement.setInt (
			1,
			routerTypeId);

		insertRouterTypeStatement.setInt (
			2,
			objectTypeId);

		insertRouterTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertRouterTypeStatement.setString (
			4,
			spec.description ());

		insertRouterTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
