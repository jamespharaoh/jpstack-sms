package wbs.sms.message.batch.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;
import wbs.sms.message.batch.model.BatchTypeRec;

@SingletonComponent ("batchLogic")
public
class BatchLogicImplementation
	implements BatchLogic {

	// dependencies

	@Inject
	BatchSubjectObjectHelper batchSubjectHelper;

	@Inject
	BatchTypeObjectHelper batchTypeHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	// implementation

	@Override
	public
	BatchSubjectRec batchSubject (
			Record<?> parent,
			String code) {

		return batchSubject (
			parent,
			code,
			code);

	}

	@Override
	public
	BatchSubjectRec batchSubject (
			Record<?> parent,
			String typeCode,
			String code) {

		// lookup existing

		BatchSubjectRec batchSubject =
			objectManager.findChildByCode (
				BatchSubjectRec.class,
				parent,
				code);

		if (batchSubject != null)
			return batchSubject;

		// or create new

		ObjectTypeRec parentType =
			objectTypeHelper.findRequired (
				objectManager.getObjectTypeId (
					parent));

		BatchTypeRec batchType =
			batchTypeHelper.findByCodeRequired (
				parentType,
				typeCode);

		return batchSubjectHelper.insert (
			batchSubjectHelper.createInstance ()

			.setCode (
				code)

			.setBatchType (
				batchType)

			.setParentType (
				parentType)

			.setParentId (
				parent.getId ()));

	}

}
