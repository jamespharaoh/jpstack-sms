package wbs.sms.spendlimit.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

public
interface SmsSpendLimitLogic {

	Optional <Long> spendCheck (
			TaskLogger parentTaskLogger,
			SmsSpendLimiterRec spendLimiter,
			NumberRec number);

	void spend (
			TaskLogger parentTaskLogger,
			SmsSpendLimiterRec spendLimiter,
			NumberRec number,
			List <MessageRec> messages,
			Long smsMessageThreadId,
			String originator);

}
