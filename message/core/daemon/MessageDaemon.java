package wbs.sms.message.core.daemon;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

@SingletonComponent ("messageDaemon")
public
class MessageDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

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

			runOnce ();

			try {

				Thread.sleep (
					sleepSecs * 1000);

			} catch (InterruptedException exception) {

				return;

			}

		}

	}

	private
	void runOnce () {

		try (

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"runOnce ()");

		) {

			try {

				expireMessages (
					taskLogger);

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					"MessageDaemon",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	private
	void expireMessages (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"expireMessages");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"MessageDaemon.expiresMessages ()",
					this);

		) {

			Collection <MessageExpiryRec> messageExpiries =
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
						taskLogger,
						message,
						MessageStatus.reportTimedOut);

					taskLogger.debugFormat (
						"Message %s expired from state %s",
						integerToDecimalString (
							message.getId ()),
						enumName (
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

					taskLogger.debugFormat (
						"Message %s expiry ignored due to state %s",
						integerToDecimalString (
							message.getId ()),
						enumName (
							oldMessageStatus));

				} else {

					// error

					throw new RuntimeException (
						stringFormat (
							"Cannot expire message %s in state %s",
							integerToDecimalString (
								message.getId ()),
							enumName (
								oldMessageStatus)));

				}

				messageExpiryHelper.remove (
					messageExpiry);

			}

			transaction.commit ();

		}

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
