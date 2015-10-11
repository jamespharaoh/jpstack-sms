package wbs.sms.tracker.model;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSimpleTrackerNumberDaoMethods {

	SmsSimpleTrackerNumberRec find (
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number);

}