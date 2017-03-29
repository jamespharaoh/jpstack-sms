package wbs.sms.message.batch.logic;

import lombok.NonNull;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.batch.model.BatchSubjectRec;

public
interface BatchLogic {

	BatchSubjectRec batchSubject (
		TaskLogger parentTaskLogger,
		Record <?> subjectObject,
		String typeCode,
		String code);

	default
	BatchSubjectRec batchSubject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> subjectObject,
			@NonNull String typeCode) {

		return batchSubject (
			parentTaskLogger,
			subjectObject,
			typeCode,
			typeCode);

	}

}
