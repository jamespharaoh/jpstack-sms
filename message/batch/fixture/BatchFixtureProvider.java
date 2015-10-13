package wbs.sms.message.batch.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;
import wbs.sms.message.batch.model.BatchTypeRec;

@PrototypeComponent ("batchFixtureProvider")
public
class BatchFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	BatchSubjectObjectHelper batchSubjectHelper;

	@Inject
	BatchTypeObjectHelper batchTypeHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		ObjectTypeRec rootType =
			objectTypeHelper.findByCode (
				GlobalId.root,
				"root");

		BatchTypeRec systemBatchType =
			batchTypeHelper.insertSpecial (
				new BatchTypeRec ()

			.setId (
				0)

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
				new BatchSubjectRec ()

			.setId (
				0)

			.setParentType (
				rootType)

			.setParentId (
				0)

			.setCode (
				"system")

			.setType (
				systemBatchType)

		);

		batchHelper.insertSpecial (
			new BatchRec ()

			.setId (
				0)

			.setParentType (
				rootType)

			.setParentId (
				0)

			.setCode (
				"system")

			.setSubject (
				systemBatchSubject)

		);

	}

}
