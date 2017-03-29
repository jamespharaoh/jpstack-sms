package wbs.sms.messageset.logic;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.core.model.NumberRec;

public
interface MessageSetLogic {

	Long sendMessageSet (
			TaskLogger parentTaskLogger,
			MessageSetRec messageSet,
			Long threadId,
			NumberRec number,
			ServiceRec service,
			AffiliateRec affiliate);

	default
	Long sendMessageSet (
			@NonNull TaskLogger parentTaskLogger,
			MessageSetRec messageSet,
			Long threadId,
			NumberRec number,
			ServiceRec service) {

		return sendMessageSet (
			parentTaskLogger,
			messageSet,
			threadId,
			number,
			service,
			null);

	}

}
