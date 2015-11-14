package wbs.sms.message.inbox.daemon;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogger.Resolution;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.platform.daemon.ThreadManager;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("receivedManager")
public
class ReceivedManager
	extends AbstractDaemonService {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandManager commandManager;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	InboxAttemptObjectHelper inboxAttemptHelper;

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	ThreadManager threadManager;

	// configuration

	final
	int sleepInterval = 1000;

	final
	int bufferSize = 128;

	final
	int numWorkerThreads = 8;

	// state

	QueueBuffer<Integer,Integer> buffer =
		new QueueBuffer<Integer,Integer> (bufferSize);

	/**
	 * Runnable class for a worker thread. Loops until the exit flag is set in
	 * the buffer, Processing messages and updating the database with the
	 * result.
	 */
	class ReceivedThread
		implements Runnable {

		String threadName;

		public
		ReceivedThread (
				String threadName) {

			this.threadName =
				threadName;

		}

		void dumpMessageInfo (
				MessageRec message) {

			log.info (
				message.getId () + " " +
				message.getNumFrom () + " " +
				message.getNumTo () + " " +
				message.getText ());

		}

		void doMessage (
				int messageId) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			InboxRec inbox =
				inboxHelper.find (
					messageId);

			MessageRec message =
				messageHelper.find (
					messageId);

			dumpMessageInfo (
				message);

			RouteRec route =
				message.getRoute ();

			if (route.getCommand () != null) {

				InboxAttemptRec inboxAttempt =
					commandManager.handle (
						inbox,
						route.getCommand (),
						Optional.fromNullable (
							message.getRef ()),
						message.getText ().getText ());

				if (inboxAttempt == null)
					throw new NullPointerException ();

			} else {

				inboxLogic.inboxNotProcessed (
					inbox,
					Optional.<ServiceRec>absent (),
					Optional.<AffiliateRec>absent (),
					Optional.<CommandRec>absent (),
					"No command for route");

			}

			transaction.commit ();

		}

		void doError (
				int messageId,
				Throwable exception) {

			log.error (
				stringFormat (
					"Error processing command for message %d",
					messageId),
				exception);

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			InboxRec inbox =
				inboxHelper.find (
					messageId);

			MessageRec message =
				inbox.getMessage ();

			RouteRec route =
				message.getRoute ();

			exceptionLogger.logThrowable (
				"daemon",
				stringFormat (
					"Route %s",
					route.getCode ()),
				exception,
				Optional.<Integer>absent (),
				Resolution.tryAgainLater);

			inboxLogic.inboxProcessingFailed (
				inbox,
				stringFormat (
					"Threw %s: %s",
					exception.getClass ().getSimpleName (),
					emptyStringIfNull (
						exception.getMessage ())));

			transaction.commit ();

		}

		@Override
		public
		void run () {

			while (true) {

				log.info ("Received thread run " + threadName);

				// get the next message

				int messageId;

				try {
					messageId = buffer.next ();
				} catch (InterruptedException e) {
					return;
				}

				// handle it

				try {

					doMessage (
						messageId);

				} catch (Exception exception) {

					doError (
						messageId,
						exception);

				}

				// remove the item from the buffer

				buffer.remove (
					messageId);

			}

		}

	}

	boolean doQuery () {

		final
		Set<Integer> activeMessageids =
			buffer.getKeys ();

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<InboxRec> inboxes =
			inboxHelper.findPendingLimit (
				transaction.now (),
				buffer.getFullSize ());

		for (
			InboxRec inbox
				: inboxes
		) {

			MessageRec message =
				inbox.getMessage ();

			if (activeMessageids.contains (
					message.getId ()))
				continue;

			buffer.add (
				message.getId (),
				message.getId ());

		}

		return inboxes.size () == buffer.getFullSize ();

	}

	class QueryThread
		implements Runnable {

		@Override
		public
		void run () {
			while (true) {

				// query database

				boolean moreMessages =
					doQuery ();

				// wait for queues to go down or for one second to elapse

				try {

					if (moreMessages) {
						buffer.waitNotFull ();
					} else {
						Thread.sleep (sleepInterval);
					}

				} catch (InterruptedException exception) {
					return;
				}

			}

		}

	}

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	/**
	 * Creates and starts all threads.
	 */
	@Override
	protected
	void createThreads () {

		// create database query thread

		Thread thread =
			threadManager.makeThread (
				new QueryThread ());

		thread.setName ("RecManA");

		thread.start ();

		registerThread (
			thread);

		// create worker threads

		for (int i = 0; i < numWorkerThreads; i ++) {

			thread =
				threadManager.makeThread (
					new ReceivedThread ("" + i));

			thread.setName ("RecMan" + i);

			thread.start ();

			registerThread (
				thread);

		}

	}

}
