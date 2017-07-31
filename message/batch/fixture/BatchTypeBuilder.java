package wbs.sms.message.batch.fixture;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelFixtureBuilderComponent;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.message.batch.metamodel.BatchTypeSpec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;

@PrototypeComponent ("batchTypeBuilder")
public
class BatchTypeBuilder
	implements ModelFixtureBuilderComponent {

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
	RecordSpec parent;

	@BuilderSource
	BatchTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull Transaction parentTransaction,
			@NonNull Builder <Transaction> builder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

			try {

				transaction.noticeFormat (
					"Create batch type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ()));

				createBatchType (
					transaction);

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

	}

	private
	void createBatchType (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createBatchType");

		) {

			// lookup subject

			String subjectTypeCode =
				camelToUnderscore (
					ifNull (
						spec.subject (),
						parent.name ()));

			ObjectTypeRec subjectType =
				objectTypeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					subjectTypeCode);

			// lookup batch

			String batchTypeCode =
				camelToUnderscore (
					spec.batch ());

			ObjectTypeRec batchType =
				objectTypeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					batchTypeCode);

			// create batch type

			batchTypeHelper.insert (
				transaction,
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

		}

	}

}
