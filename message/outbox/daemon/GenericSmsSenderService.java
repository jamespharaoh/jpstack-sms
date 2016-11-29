package wbs.sms.message.outbox.daemon;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.LongStream;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;
import wbs.sms.route.sender.model.SenderRec;

@Accessors (fluent = true)
@PrototypeComponent ("genericSmsSenderService")
public
class GenericSmsSenderService
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsOutboxMonitor outboxMonitor;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	OutboxObjectHelper smsOutboxHelper;

	@SingletonDependency
	SmsOutboxLogic smsOutboxLogic;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <GenericSmsSender> genericSmsSenderProvider;

	// properties

	@Getter @Setter
	protected
	SmsSenderHelper <?> smsSenderHelper;

	@Getter @Setter
	int maxTries = 10;

	@Getter @Setter
	int retryTimeMs = 10;

	@Getter @Setter
	int waitTimeMs = 1000;

	@Getter @Setter
	int threadsPerRoute = 4;

	// state

	List <RouteSenderService> routeSenderServices =
		new ArrayList<> ();

	// implementation

	@Override
	protected
	void createThreads () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				stringFormat (
					"%s.createThreads ()",
					getClass ().getSimpleName ()),
				this);

		// get a list of routes

		SenderRec sender =
			senderHelper.findByCodeRequired (
				GlobalId.root,
				smsSenderHelper.senderCode ());

		Set <RouteRec> smsRoutes =
			sender.getRoutes ();

		// and for each one...

		for (
			RouteRec smsRoute
				: smsRoutes
		) {

			RouteSenderService routeSenderService =
				new RouteSenderService ()

				.smsRouteId (
					smsRoute.getId ())

				.start ();

			routeSenderServices.add (
				routeSenderService);

		}

	}

	@Accessors (fluent = true)
	class RouteSenderService {

		@Getter @Setter
		long smsRouteId;

		long claimedMessages = 0;

		Queue <Long> messageQueue =
			new LinkedList<> ();

		RouteSenderService start () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					stringFormat (
						"%s.start ()",
						joinWithFullStop (
							"GenericSmsSenderService",
							"RouteSenderService")),
					this);

			RouteRec smsRoute =
				smsRouteHelper.findRequired (
					smsRouteId);

			createThread (
				stringFormat (
					"sms-route-%s-claim",
					underscoreToHyphen (
						smsRoute.getCode ())),
				this::messageClaimLoop);

			LongStream.range (0, threadsPerRoute).forEach (
				threadIndex ->
					createThread (
						stringFormat (
							"sms-route-%s-send-%s",
							underscoreToHyphen (
								smsRoute.getCode ()),
							integerToDecimalString (
								threadIndex)),
						this::messageSendLoop));

			return this;

		}

		void messageClaimLoop () {

			for (;;) {

				try {

					outboxMonitor.waitForRoute (
						smsRouteId);

					claimAllMessages ();

				} catch (InterruptedException interruptedException) {
					return;
				}

			}

		}

		void claimAllMessages ()
			throws InterruptedException {

			for (;;) {

				// wait for threads to become free

				long numAvailable =
					waitForAvailableSenders ();

				// claim some messages

				long numClaimed =
					claimSomeMessages (
						numAvailable);

				synchronized (this) {

					claimedMessages +=
						numClaimed;

					notifyAll ();

				}

				if (
					equalToZero (
						numClaimed)
				) {
					return;
				}

			}

		}

		synchronized
		long waitForAvailableSenders ()
			throws InterruptedException {

			while (
				integerEqualSafe (
					claimedMessages,
					threadsPerRoute)
			) {
				wait ();
			}

			long numAvailable =
				+ threadsPerRoute
				- claimedMessages;

			return numAvailable;

		}

		long claimSomeMessages (
				long numToGet) {

			// begin transaction

			try (

				Transaction transaction =
					database.beginReadWrite (
						stringFormat (
							"%s.claimMessages ()",
							joinWithFullStop (
								"GenericSmsSenderService",
								"RouteSenderService")),
						this);

			) {

				// get some messages

				RouteRec route =
					smsRouteHelper.findRequired (
						smsRouteId);

				List <OutboxRec> smsOutboxes =
					smsOutboxHelper.findNextLimit (
						transaction.now (),
						route,
						numToGet);

				// return if empty

				if (
					collectionIsEmpty (
						smsOutboxes)
				) {
					return 0l;
				}

				// mark them as sending

				smsOutboxes.forEach (
					smsOutbox -> {

					smsOutbox

						.setSending (
							transaction.now ());

				});

				// store their ids

				List <Long> messageIds =
					iterableMapToList (
						OutboxRec::getId,
						smsOutboxes);

				// commit and add them to the queue

				transaction.commit ();

				messageQueue.addAll (
					messageIds);

				return messageIds.size ();

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"daemon",
					classNameSimple (
						this.getClass ()),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

				return 0l;

			}

		}

		void messageSendLoop () {

			for (;;) {

				long smsMessageId;

				try {

					smsMessageId =
						getOneMessageToSend ();

				} catch (InterruptedException interruptedException) {
					return;
				}

				TaskLogger taskLogger =
					logContext.createTaskLogger (
						"messageSendLoop");

				sendOneMessage (
					taskLogger,
					smsMessageId);

				synchronized (this) {

					claimedMessages --;

					notifyAll ();

				}

			}

		}

		synchronized
		long getOneMessageToSend ()
			throws InterruptedException {

			while (
				collectionIsEmpty (
					messageQueue)
			) {
				wait ();
			}

			long messageId =
				messageQueue.remove ();

			notifyAll ();

			return messageId;

		}

		void sendOneMessage (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long messageId) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendOneMessage");

			genericSmsSenderProvider.get ()

				.smsSenderHelper (
					smsSenderHelper)

				.smsMessageId (
					messageId)

				.send (
					taskLogger);

		}

	}

}
