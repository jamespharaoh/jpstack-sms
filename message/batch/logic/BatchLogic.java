package wbs.sms.message.batch.logic;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.sms.message.batch.model.BatchSubjectRec;

public
interface BatchLogic {

	BatchSubjectRec batchSubject (
			Transaction parentTransaction,
			Record <?> subjectObject,
			String typeCode,
			String code);

	default
	BatchSubjectRec batchSubject (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> subjectObject,
			@NonNull String typeCode) {

		return batchSubject (
			parentTransaction,
			subjectObject,
			typeCode,
			typeCode);

	}

}
