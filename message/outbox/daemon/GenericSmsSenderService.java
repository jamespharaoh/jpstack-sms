package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isEmptyString;
import static wbs.framework.utils.etc.Misc.isZero;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.underscoreToHyphen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
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

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsOutboxMonitor outboxMonitor;

	@Inject
	SenderObjectHelper senderHelper;

	@Inject
	OutboxObjectHelper smsOutboxHelper;

	@Inject
	SmsOutboxLogic smsOutboxLogic;

	@Inject
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@Inject
	Provider<GenericSmsSender> genericSmsSenderProvider;

	// properties

	@Getter @Setter
	protected
	SmsSenderHelper<?> smsSenderHelper;

	@Getter @Setter
	int maxTries = 10;

	@Getter @Setter
	int retryTimeMs = 10;

	@Getter @Setter
	int waitTimeMs = 1000;

	@Getter @Setter
	int threadsPerRoute = 4;

	// state

	List<RouteSenderService> routeSenderServices =
		new ArrayList<> ();

	// implementation

	@Override
	protected
	void createThreads () {

System.out.println ("XXX CREATE THREADS");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				stringFormat (
					"%s.createThreads ()",
					getClass ().getSimpleName ()),
				this);

		// get a list of routes

System.out.println ("XXX SENDER HELPER: " + smsSenderHelper);

		SenderRec sender =
			senderHelper.findByCodeRequired (
				GlobalId.root,
				smsSenderHelper.senderCode ());

		Set<RouteRec> smsRoutes =
			sender.getRoutes ();

		// and for each one...

		for (
			RouteRec smsRoute
				: smsRoutes
		) {

System.out.println ("XXX ROUTE " + smsRoute.getCode ());

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

		Queue<Long> messageQueue =
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
							threadIndex),
						this::messageSendLoop));

			return this;

		}

		void messageClaimLoop () {

			for (;;) {

				try {

System.out.println ("XXX wait for route " + smsRouteId);

					outboxMonitor.waitForRoute (
						smsRouteId);

System.out.println ("XXX wait for route complete");

System.out.println ("XXX claim all messages");

					claimAllMessages ();

System.out.println ("XXX claim all messages complete");

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
					isZero (
						numClaimed)
				) {
					return;
				}

			}

		}

		synchronized
		long waitForAvailableSenders ()
			throws InterruptedException {

System.out.println ("XXX wait for senders");

			while (
				equal (
					claimedMessages,
					threadsPerRoute)
			) {
				wait ();
			}

			long numAvailable =
				+ threadsPerRoute
				- claimedMessages;

System.out.println ("XXX num available " + numAvailable);

			return numAvailable;

		}

		long claimSomeMessages (
				long numToGet) {

			// begin transaction

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					stringFormat (
						"%s.claimMessages ()",
						joinWithFullStop (
							"GenericSmsSenderService",
							"RouteSenderService")),
					this);

			// get some messages

			RouteRec route =
				smsRouteHelper.findRequired (
					smsRouteId);

			List<OutboxRec> outboxes =
				smsOutboxHelper.findNextLimit (
					transaction.now (),
					route,
					numToGet);

			if (
				isEmpty (
					outboxes)
			) {
				return 0l;
			}

			// mark them as sending

			List<Long> messageIds =
				new ArrayList<Long> ();

			for (
				OutboxRec smsOutbox
					: outboxes
			) {

				smsOutbox

					.setSending (
						transaction.now ());

				messageIds.add (
					(long) smsOutbox.getId ());

			}

			// commit and add them to the queue

			transaction.commit ();

			messageQueue.addAll (
				messageIds);

			return messageIds.size ();

		}

		void messageSendLoop () {

			for (;;) {

				long smsMessageId;

				try {

System.out.println ("XXX wait for messages to send");

					smsMessageId =
						getOneMessageToSend ();

System.out.println ("XXX got message " + smsMessageId);

				} catch (InterruptedException interruptedException) {
					return;
				}

				sendOneMessage (
					smsMessageId);

			}

		}

		synchronized
		long getOneMessageToSend ()
			throws InterruptedException {

			while (
				isEmpty (
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
				long messageId) {

			genericSmsSenderProvider.get ()

				.smsSenderHelper (
					smsSenderHelper)

				.smsMessageId (
					messageId)

				.send ();

		}

	}

}
