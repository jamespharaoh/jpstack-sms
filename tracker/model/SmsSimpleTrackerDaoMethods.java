package wbs.sms.tracker.model;

import java.util.List;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

public
interface SmsSimpleTrackerDaoMethods {

	List<MessageRec> findMessages (
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number);

}