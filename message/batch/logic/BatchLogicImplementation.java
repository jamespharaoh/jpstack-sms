package wbs.sms.message.batch.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// implementation

	@Override
	public
	BatchSubjectRec batchSubject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record<?> parent,
			@NonNull String typeCode,
			@NonNull String code) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"batchSubject");

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
			taskLogger,
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
