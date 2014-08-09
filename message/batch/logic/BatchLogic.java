package wbs.sms.message.batch.logic;

import wbs.framework.record.Record;
import wbs.sms.message.batch.model.BatchSubjectRec;

public
interface BatchLogic {

	BatchSubjectRec batchSubject (
		Record<?> subjectObject,
		String typeCode);

	BatchSubjectRec batchSubject (
		Record<?> subjectObject,
		String typeCode,
		String code);
}
