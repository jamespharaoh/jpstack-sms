package wbs.sms.message.batch.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;

import java.sql.SQLException;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.message.batch.metamodel.BatchTypeSpec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;

@Log4j
@PrototypeComponent ("batchTypeBuilder")
@ModelMetaBuilderHandler
public
class BatchTypeBuilder {

	// dependencies

	@Inject
	BatchTypeObjectHelper batchTypeHelper;

	@Inject
	Database database;

	@Inject
	EntityHelper entityHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

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

			log.info (
				stringFormat (
					"Create batch type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())));

			createBatchType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating batch type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createBatchType ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"BatchTypeBuilder.createBatchType ()",
				this);

		// lookup subject

		String subjectTypeCode =
			camelToUnderscore (
				ifNull (
					spec.subject (),
					parent.name ()));

		ObjectTypeRec subjectType =
			objectTypeHelper.findByCodeRequired (
				GlobalId.root,
				subjectTypeCode);

		// lookup batch

		String batchTypeCode =
			camelToUnderscore (
				spec.batch ());

		ObjectTypeRec batchType =
			objectTypeHelper.findByCodeRequired (
				GlobalId.root,
				batchTypeCode);

		// create batch type

		batchTypeHelper.insert (
			batchTypeHelper.createInstance ()

			.setSubjectType (
				subjectType)

			.setCode (
				simplifyToCodeRequired (
					spec.name ()))

			.setName (
				spec.name ())

			.setDescription (
				spec.description ())

			.setBatchType (
				batchType)

		);

		transaction.commit ();

	}

}
