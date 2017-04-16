package wbs.sms.message.batch.fixture;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.message.batch.metamodel.BatchTypeSpec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;

@Log4j
@PrototypeComponent ("batchTypeBuilder")
@ModelMetaBuilderHandler
public
class BatchTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	BatchTypeObjectHelper batchTypeHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	EntityHelper entityHelper;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	BatchTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

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

			createBatchType (
				taskLogger);

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
	void createBatchType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createBatchType");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"BatchTypeBuilder.createBatchType ()",
					this);

		) {

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
				taskLogger,
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

}
