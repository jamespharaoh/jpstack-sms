package wbs.sms.message.inbox.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.Tries;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.platform.daemon.ThreadManager;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.daemon.CommandHandler.Status;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("receivedManager")
public
class ReceivedManager
	extends AbstractDaemonService {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandManager commandManager;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

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

	final
	int sleepInterval = 1000;

	final
	int bufferSize = 128;

	final
	int numWorkerThreads = 8;

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

			class MessageStuff {
				MessageRec message;
				RouteRec route;
				Integer commandId;
			}

			MessageStuff messageStuff =
				new MessageStuff ();

			// begin transaction

			@Cleanup
			Transaction transaction0 =
				database.beginReadWrite ();

			// save some information about the message

			messageStuff.message =
				messageHelper.find (messageId);

			messageStuff.message.getText ().getText ();

			messageStuff.route =
				messageStuff.message.getRoute ();

			if (messageStuff.route.getCommand () != null) {

				messageStuff.commandId =
					messageStuff.route.getCommand ().getId ();

			}

			// update the inbox tries and retrytime

			InboxRec inbox =
				inboxHelper.find (
					messageId);

			inbox.setTries (
				inbox.getTries () + 1);

			Calendar calendar =
				new GregorianCalendar ();

			calendar.add (
				Calendar.SECOND,
				inbox.getTries ());

			inbox

				.setRetryTime (
					calendar.getTime ());

			// commit transaction

			transaction0.commit ();

			final
			ReceivedMessageImpl receivedMessage =
				new ReceivedMessageImpl (
					null,
					messageStuff.message.getId (),
					messageStuff.message.getText ().getText (),
					0);

			dumpMessageInfo (
				messageStuff.message);

			Status status = null;

			// mark as not processed if there is no command handler

			if (messageStuff.commandId == null) {

				doSaveResult (
					Status.notprocessed,
					receivedMessage);

				log.warn (
					"Message not processed because there is no command.");

				return;
			}

			// mark not processed if stop message on adult route (?)
			// TODO wtf is this? should not be here

			if (messageStuff.route.getAvRequired ()
					&& "0".equals (messageStuff.message.getAdultVerified ())
					&& ! isStopKeyword (messageStuff.message.getText ().getText ())) {

				doSaveResult (
					CommandHandler.Status.notprocessed,
					receivedMessage);

				log.warn (
					"Number is not adult verified and this route requires verification.");

				return;

			}

			// run the message through the handler

			try {

				status =
					commandManager.handle (
						messageStuff.commandId,
						receivedMessage);

				log.debug (
					stringFormat (
						"Command handler finished for message %d",
						messageId));

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error processing command for message %d",
						messageId),
					exception);

				exceptionLogic.logThrowable (
					"daemon",
					"Route " + messageStuff.route.getCode (),
					exception,
					null,
					false);
			}

			if (status != null)
				doSaveResult (
					status,
					receivedMessage);

		}

		void doSaveResult (
				CommandHandler.Status status,
				ReceivedMessageImpl receivedMessage) {

			MessageStatus messageStatus =
				status == CommandHandler.Status.processed
					? MessageStatus.processed
					: MessageStatus.notProcessed;

			Tries tries =
				new Tries ();

			while (tries.next ()) {

				try {

					trySaveResult (
						messageStatus,
						receivedMessage);

					tries.done ();

				} catch (RuntimeException exception) {

					log.error (
						stringFormat (
							"Error processing message %d",
							receivedMessage.getMessageId ()),
						exception);

					tries.error (
						exception);

				}

			}

		}

		private
		void trySaveResult (
				MessageStatus status,
				ReceivedMessageImpl receivedMessage) {

			@Cleanup
			Transaction transaction1 =
				database.beginReadWrite ();

			MessageRec message =
				messageHelper.find (
					receivedMessage.getMessageId ());

			ServiceRec service =
				receivedMessage.getServiceId () == null
					? null
					: serviceHelper.find (
						receivedMessage.getServiceId ());

			AffiliateRec affiliate =
				receivedMessage.getAffiliateId () == null
					? null
					: affiliateHelper.find (
						receivedMessage.getAffiliateId ());

			if (status == MessageStatus.processed) {

				inboxLogic.inboxProcessed (
					message,
					service,
					affiliate,
					null);

			} else {

				inboxLogic.inboxNotProcessed (
					message,
					service,
					affiliate,
					null,
					"Command handler returned not processed");

			}

			transaction1.commit ();

		}

		// TODO could be done much better as a regexp
		// TODO wtf is this doing here?
		boolean isStopKeyword (
				String text) {

			if (text == null || text.length () < 4)
				return false;
			else
				return text.toLowerCase ().startsWith ("stop");

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

				doMessage (messageId);

				// remove the item from the buffer

				buffer.remove (messageId);

			}

		}

	}

	boolean doQuery () {

		final
		Set<Integer> activeMessageids =
			buffer.getKeys ();

		List<InboxRec> indexes;

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		indexes =
			inboxHelper.findRetryLimit (
				buffer.getFullSize ());

		for (InboxRec inbox
				: indexes) {

			MessageRec message =
				inbox.getMessage ();

			if (activeMessageids.contains (
					message.getId ()))
				continue;

			buffer.add (
				message.getId (),
				message.getId ());

		}

		return indexes.size () == buffer.getFullSize ();

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
