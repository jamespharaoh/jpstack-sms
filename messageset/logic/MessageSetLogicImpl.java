package wbs.sms.messageset.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.MajorRecord;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("messageSetLogic")
public
class MessageSetLogicImpl
	implements MessageSetLogic {

	@Inject
	ObjectManager objectManager;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	MessageSetRec findMessageSet (
			MajorRecord<?> parent,
			String code) {

		return objectManager.findChildByCode (
			MessageSetRec.class,
			parent,
			code);

	}

	@Override
	public
	Integer sendMessageSet (
			@NonNull MessageSetRec messageSet,
			Integer threadId,
			@NonNull NumberRec number,
			ServiceRec service,
			AffiliateRec affiliate) {

		for (MessageSetMessageRec messageSetMessage
				: messageSet.getMessages ().values ()) {

			MessageRec message =
				messageSender.get ()
					.threadId (threadId)
					.number (number)
					.messageString (messageSetMessage.getMessage ())
					.numFrom (messageSetMessage.getNumber ())
					.route (messageSetMessage.getRoute ())
					.service (service)
					.affiliate (affiliate)
					.send ();

			if (threadId == null)
				threadId = message.getId ();

		}

		return threadId;

	}

	@Override
	public
	Integer sendMessageSet (
			MessageSetRec messageSet,
			Integer threadId,
			NumberRec number,
			ServiceRec service) {

		return sendMessageSet (
			messageSet,
			threadId,
			number,
			service,
			null);

	}

}
