package wbs.sms.tracker.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSimpleTrackerNumberDaoMethods {

	SmsSimpleTrackerNumberRec find (
			Transaction parentTransaction,
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number);

}