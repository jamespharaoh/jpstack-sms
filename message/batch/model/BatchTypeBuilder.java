package wbs.sms.message.batch.model;

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
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMetaBuilderHandler;
import wbs.framework.entity.model.ModelMetaSpec;

@PrototypeComponent ("batchTypeBuilder")
@ModelMetaBuilderHandler
public
class BatchTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	BatchTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createBatchType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating batch type %s.%s",
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
	void createBatchType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		// allocate id

		@Cleanup
		PreparedStatement nextBatchTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('batch_type_id_seq')"));

		ResultSet batchTypeIdResultSet =
			nextBatchTypeIdStatement.executeQuery ();

		batchTypeIdResultSet.next ();

		int batchTypeId =
			batchTypeIdResultSet.getInt (
				1);

		// lookup subject

		String subjectTypeCode =
			ifNull (
				spec.subject (),
				parent.name ());

		Model subjectModel =
			entityHelper.modelsByName ().get (
				subjectTypeCode);

		if (subjectModel == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					subjectTypeCode));

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
			subjectModel.objectTypeCode ());

		ResultSet getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int subjectObjectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		// lookup batch

		Model batchModel =
			entityHelper.modelsByName ().get (
				spec.batch ());

		if (batchModel == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					spec.batch ()));

		}

		getObjectTypeIdStatement.setString (
			1,
			batchModel.objectTypeCode ());

		getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int batchObjectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		// create batch type

		@Cleanup
		PreparedStatement insertBatchTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO batch_type (",
						"id, ",
						"subject_object_type_id, ",
						"code, ",
						"batch_object_type_id, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertBatchTypeStatement.setInt (
			1,
			batchTypeId);

		insertBatchTypeStatement.setInt (
			2,
			subjectObjectTypeId);

		insertBatchTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertBatchTypeStatement.setInt (
			4,
			batchObjectTypeId);

		insertBatchTypeStatement.setString (
			5,
			spec.description ());

		insertBatchTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
