package wbs.sms.message.batch.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		ObjectTypeRec rootType =
			objectTypeHelper.findByCodeRequired (
				GlobalId.root,
				"root");

		BatchTypeRec systemBatchType =
			batchTypeHelper.insertSpecial (
				taskLogger,
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
				taskLogger,
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
			taskLogger,
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
