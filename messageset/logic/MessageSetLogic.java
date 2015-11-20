package wbs.sms.messageset.logic;

import wbs.framework.record.MajorRecord;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.core.model.NumberRec;

public
interface MessageSetLogic {

	Integer sendMessageSet (
			MessageSetRec messageSet,
			Integer threadId,
			NumberRec number,
			ServiceRec service,
			AffiliateRec affiliate);

	Integer sendMessageSet (
			MessageSetRec messageSet,
			Integer threadId,
			NumberRec number,
			ServiceRec service);

	MessageSetRec findMessageSet (
			MajorRecord<?> parent,
			String code);

}
