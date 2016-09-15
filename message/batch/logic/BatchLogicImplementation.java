package wbs.sms.message.batch.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;
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

	// singleton dependencies

	@SingletonDependency
	BatchSubjectObjectHelper batchSubjectHelper;

	@SingletonDependency
	BatchTypeObjectHelper batchTypeHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
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

		Optional<BatchSubjectRec> batchSubjectOptional =
			batchSubjectHelper.findByCode (
				parent,
				code);

		if (
			optionalIsPresent (
				batchSubjectOptional)
		) {
			return batchSubjectOptional.get ();
		}

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
