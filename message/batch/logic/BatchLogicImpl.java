package wbs.sms.message.batch.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.batch.model.BatchTypeObjectHelper;
import wbs.sms.message.batch.model.BatchTypeRec;

@SingletonComponent ("batchLogic")
public
class BatchLogicImpl
	implements BatchLogic {

	@Inject
	BatchTypeObjectHelper batchTypeHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

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
			objectTypeHelper.find (
				objectManager.getObjectTypeId (parent));

		BatchTypeRec batchType =
			batchTypeHelper.findByCode (
				parentType,
				typeCode);

		if (batchType == null) {

			throw new RuntimeException (
				stringFormat (
					"Batch type not found %s.%s",
					parentType.getCode (),
					typeCode));

		}

		return objectManager.insert (
			new BatchSubjectRec ()

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
