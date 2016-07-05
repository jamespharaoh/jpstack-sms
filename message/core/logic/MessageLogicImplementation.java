package wbs.sms.message.core.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.core.model.MessageDao;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.number.core.logic.NumberLogic;

@SingletonComponent ("messageLogic")
public
class MessageLogicImplementation
	implements MessageLogic {

	// dependencies

	@Inject
	MessageDao messageDao;

	@Inject
	NumberLogic numberLogic;

	@Inject
	OutboxObjectHelper outboxHelper;

	// implementation

	@Override
	public
	boolean isChatMessage (
			MessageRec message) {

		// TODO hard coded for now

		return message
			.getService ()
			.getParentType ()
			.getCode ()
			.equals ("chat");

	}

	@Override
	public
	void messageStatus (
			MessageRec message,
			MessageStatus newStatus) {

		message.setStatus (newStatus);

		// TODO wtf?
		// store delivery status for number
		// following code shouldn't be here really
		if (isChatMessage (message)) {

			numberLogic.updateDeliveryStatusForNumber (
				message.getNumTo (),
				newStatus);

		}

	}

	@Override
	public
	void blackListMessage (
			MessageRec message) {

		// check message state

		if (message.getStatus () == MessageStatus.pending) {

			// lookup outbox

			OutboxRec outbox =
				outboxHelper.findOrNull (
					message.getId ());

			// check message is not being sent

			if (outbox.getSending () != null) {

				throw new RuntimeException (
					"Message is being sent");

			}

			// blacklist message

			messageStatus (
				message,
				MessageStatus.blacklisted);

			// remove outbox

			outboxHelper.remove (
				outbox);

		} else if (message.getStatus () == MessageStatus.held) {

			// blacklist message

			messageStatus (
				message,
				MessageStatus.blacklisted);

		} else {

			throw new RuntimeException (
				"Message is not pending/held");

		}

	}

}
