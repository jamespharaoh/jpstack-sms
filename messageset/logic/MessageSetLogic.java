package wbs.sms.messageset.logic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

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
			Optional <Long> threadId,
			NumberRec number,
			ServiceRec service,
			Optional <AffiliateRec> affiliate);

	default
	Long sendMessageSet (
			@NonNull Transaction parentTransaction,
			@NonNull MessageSetRec messageSet,
			@NonNull Optional <Long> threadId,
			@NonNull NumberRec number,
			@NonNull ServiceRec service) {

		return sendMessageSet (
			parentTransaction,
			messageSet,
			threadId,
			number,
			service,
			optionalAbsent ());

	}

}
