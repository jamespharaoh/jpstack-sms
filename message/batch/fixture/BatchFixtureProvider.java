package wbs.sms.message.batch.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;
import wbs.sms.message.batch.model.BatchTypeRec;

@PrototypeComponent ("batchFixtureProvider")
public
class BatchFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	BatchSubjectObjectHelper batchSubjectHelper;

	@SingletonDependency
	BatchTypeObjectHelper batchTypeHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			ObjectTypeRec rootType =
				objectTypeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"root");

			BatchTypeRec systemBatchType =
				batchTypeHelper.insertSpecial (
					transaction,
					batchTypeHelper.createInstance ()

				.setId (
					0l)

				.setSubjectType (
					rootType)

				.setCode (
					"system")

				.setName (
					"System")

				.setDescription (
					"System")

				.setBatchType (
					rootType)

			);

			BatchSubjectRec systemBatchSubject =
				batchSubjectHelper.insertSpecial (
					transaction,
					batchSubjectHelper.createInstance ()

				.setId (
					0l)

				.setParentType (
					rootType)

				.setParentId (
					0l)

				.setCode (
					"system")

				.setBatchType (
					systemBatchType)

			);

			batchHelper.insertSpecial (
				transaction,
				batchHelper.createInstance ()

				.setId (
					0l)

				.setParentType (
					rootType)

				.setParentId (
					0l)

				.setCode (
					"system")

				.setSubject (
					systemBatchSubject)

			);

		}

	}

}
