package wbs.sms.messageset.logic;

import lombok.NonNull;

import wbs.framework.database.Transaction;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.core.model.NumberRec;

public
interface MessageSetLogic {

	Long sendMessageSet (
			Transaction parentTransaction,
			MessageSetRec messageSet,
			Long threadId,
			NumberRec number,
			ServiceRec service,
			AffiliateRec affiliate);

	default
	Long sendMessageSet (
			@NonNull Transaction parentTransaction,
			@NonNull MessageSetRec messageSet,
			Long threadId,
			@NonNull NumberRec number,
			ServiceRec service) {

		return sendMessageSet (
			parentTransaction,
			messageSet,
			threadId,
			number,
			service,
			null);

	}

}
