package wbs.sms.message.core.daemon;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.daemon.MessageRetrier;
import wbs.sms.core.daemon.MessageRetrierFactory;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageExpiryObjectHelper;
import wbs.sms.message.core.model.MessageExpiryRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;

@Log4j
@SingletonComponent ("messageDaemon")
public
class MessageDaemon
	extends AbstractDaemonService {

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	MessageExpiryObjectHelper messageExpiryHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	OutboxLogic outboxLogic;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	@Getter
	Map<String,MessageRetrierFactory> messageRetrierFactories =
		Collections.emptyMap ();

	@Inject
	public
	void setMessageRetrierFactories (
			Map<String,MessageRetrierFactory> messageRetrierFactories) {

		this.messageRetrierFactories =
			messageRetrierFactories;

	}

	@Getter @Setter
	int sleepSecs = 60;

	@Getter @Setter
	int batchSize = 100;

	@Override
	protected
	void runService () {

		for (;;) {

			try {

				expireMessages ();

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"MessageDaemon",
					exception,
					null,
					false);

			}

			try {

				Thread.sleep (
					sleepSecs * 1000);

			} catch (InterruptedException e) {

				return;

			}

		}

	}

	private void expireMessages () {

		@Cleanup Transaction transaction =
			database.beginReadWrite ();

		Collection<MessageExpiryRec> messageExpiries =
			messageExpiryHelper.findPendingLimit (
				batchSize);

		if (messageExpiries.size () == 0)
			return;

		for (MessageExpiryRec messageExpiry
				: messageExpiries) {

			MessageRec message =
				messageExpiry.getMessage ();

			MessageStatus oldMessageStatus =
				message.getStatus ();

			if (in (oldMessageStatus,
					MessageStatus.sent,
					MessageStatus.submitted)) {

				messageLogic.messageStatus (
					message,
					MessageStatus.reportTimedOut);

				// smsUtils.messageRebill(message,MessageStatus.reportTimedOut);

				log.debug (
					stringFormat (
						"Message %s expired from state %s",
						message.getId (),
						oldMessageStatus));

			} else if (in (oldMessageStatus,
					MessageStatus.delivered,
					MessageStatus.undelivered)) {

				log.debug (
					stringFormat (
						"Message %s expiry ignored due to state %s",
						message.getId (),
						oldMessageStatus));

			} else {

				throw new RuntimeException (
					stringFormat (
						"Cannot expire message %s in state %s",
						message.getId (),
						oldMessageStatus));

			}

			messageExpiryHelper.remove (
				messageExpiry);

		}

		transaction.commit ();

	}

	@Override
	protected
	String getThreadName () {
		return "MsgDaemon";
	}

	private
	Map<String,MessageRetrier> messageRetriers =
		new HashMap<String,MessageRetrier> ();

	@PostConstruct
	public
	void afterPropertiesSet () {

		for (Map.Entry<String,MessageRetrierFactory> entry
				: messageRetrierFactories.entrySet ()) {

			messageRetriers.putAll (
				entry.getValue ()
					.getMessageRetriersByMessageTypeCode ());

		}

	}

}
