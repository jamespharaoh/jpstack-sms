package wbs.sms.core.daemon;


import wbs.platform.text.model.TextRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

/**
 * Provides an interface for the message retry feature to the business logic for
 * resending a message.
 *
 * @author James Pharaoh
 * @see MessageRetrierFactory
 */
public
interface MessageRetrier {

	/**
	 * Retries the message associated with "retry".
	 *
	 * This should create a new MessageRec and OutboxRec, along with any
	 * associated entities.
	 *
	 *
	 * @param retry
	 *            the MessageRec associated with the message to be retried.
	 */
	MessageRec messageRetry (
			MessageRec retry,
			RouteRec rec,
			TextRec textRec);

}
