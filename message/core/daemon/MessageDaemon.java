package wbs.sms.message.core.daemon;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.daemon.MessageRetrier;
import wbs.sms.core.daemon.MessageRetrierFactory;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageExpiryObjectHelper;
import wbs.sms.message.core.model.MessageExpiryRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;

@Log4j
@SingletonComponent ("messageDaemon")
public
class MessageDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	MessageExpiryObjectHelper messageExpiryHelper;

	@SingletonDependency
	Map <String, MessageRetrierFactory> messageRetrierFactories;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// properties

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

				exceptionLogger.logThrowable (
					"daemon",
					"MessageDaemon",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

			try {

				Thread.sleep (
					sleepSecs * 1000);

			} catch (InterruptedException exception) {

				return;

			}

		}

	}

	private
	void expireMessages () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageDaemon.expiresMessages ()",
				this);

		Collection<MessageExpiryRec> messageExpiries =
			messageExpiryHelper.findPendingLimit (
				transaction.now (),
				batchSize);

		if (messageExpiries.size () == 0)
			return;

		for (
			MessageExpiryRec messageExpiry
				: messageExpiries
		) {

			MessageRec message =
				messageExpiry.getMessage ();

			MessageStatus oldMessageStatus =
				message.getStatus ();

			if (
				enumInSafe (
					oldMessageStatus,
					MessageStatus.sent,
					MessageStatus.submitted)
			) {

				// perform expiry

				messageLogic.messageStatus (
					message,
					MessageStatus.reportTimedOut);

				log.debug (
					stringFormat (
						"Message %s expired from state %s",
						message.getId (),
						oldMessageStatus));

			} else if (
				enumInSafe (
					oldMessageStatus,
					MessageStatus.delivered,
					MessageStatus.undelivered,
					MessageStatus.manuallyUndelivered,
					MessageStatus.manuallyDelivered)
			) {

				// ignore expiry

				log.debug (
					stringFormat (
						"Message %s expiry ignored due to state %s",
						message.getId (),
						oldMessageStatus));

			} else {

				// error

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
	Map <String, MessageRetrier> messageRetriers =
		new HashMap<> ();

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		for (
			Map.Entry <String, MessageRetrierFactory> entry
				: messageRetrierFactories.entrySet ()
		) {

			messageRetriers.putAll (
				entry.getValue ()
					.getMessageRetriersByMessageTypeCode ());

		}

	}

}
