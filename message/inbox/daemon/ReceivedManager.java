package wbs.sms.message.inbox.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrDefault;
import static wbs.utils.etc.LogicUtils.parseBooleanYesNoRequired;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.platform.service.model.ServiceObjectHelper;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.thread.ThreadManager;

@SingletonComponent ("receivedManager")
public
class ReceivedManager
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@WeakSingletonDependency
	CommandManager commandManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	InboxAttemptObjectHelper inboxAttemptHelper;

	@SingletonDependency
	InboxObjectHelper inboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	ThreadManager threadManager;

	@SingletonDependency
	WbsConfig wbsConfig;

	// configuration

	final
	int sleepInterval = 1000;

	final
	int bufferSize = 128;

	final
	int numWorkerThreads = 8;

	// state

	QueueBuffer<Long,Long> buffer =
		new QueueBuffer<> (
			bufferSize);

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
				@NonNull TaskLogger taskLogger,
				@NonNull MessageRec message) {

			taskLogger.noticeFormat (
				"%s %s %s %s",
				integerToDecimalString (
					message.getId ()),
				message.getNumFrom (),
				message.getNumTo (),
				message.getText ().getText ());

		}

		void doMessage (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long messageId) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doMessage");

			try (

				Transaction transaction =
					database.beginReadWrite (
						"ReceivedManager.ReceivedThread.doMessage (messageId)",
						this);

			) {

				InboxRec inbox =
					inboxHelper.findRequired (
						messageId);

				MessageRec message =
					messageHelper.findRequired (
						messageId);

				dumpMessageInfo (
					taskLogger,
					message);

				RouteRec route =
					message.getRoute ();

				if (route.getCommand () != null) {

					InboxAttemptRec inboxAttempt =
						commandManager.handle (
							taskLogger,
							inbox,
							route.getCommand (),
							Optional.fromNullable (
								message.getRef ()),
							message.getText ().getText ());

					if (inboxAttempt == null)
						throw new NullPointerException ();

				} else {

					smsInboxLogic.inboxNotProcessed (
						taskLogger,
						inbox,
						optionalAbsent (),
						optionalAbsent (),
						optionalAbsent (),
						"No command for route");

				}

				transaction.commit ();

			}

		}

		void doError (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long messageId,
				@NonNull Throwable exception) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doError");

			taskLogger.errorFormatException (
				exception,
				"Error processing command for message %s",
				integerToDecimalString (
					messageId));

			try (

				Transaction transaction =
					database.beginReadWrite (
						stringFormat (
							"%s (%s)",
							joinWithFullStop (
								"ReceivedManager",
								"ReceivedThread",
								"doError"),
							joinWithCommaAndSpace (
								stringFormat (
									"messageId = %s",
									integerToDecimalString (
										messageId)),
								stringFormat (
									"exception = %s",
									exception.getClass ().getSimpleName ()))),
						this);

			) {

				InboxRec inbox =
					inboxHelper.findRequired (
						messageId);

				MessageRec message =
					inbox.getMessage ();

				RouteRec route =
					message.getRoute ();

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					stringFormat (
						"Route %s",
						route.getCode ()),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

				smsInboxLogic.inboxProcessingFailed (
					taskLogger,
					inbox,
					stringFormat (
						"Threw %s: %s",
						exception.getClass ().getSimpleName (),
						emptyStringIfNull (
							exception.getMessage ())));

				transaction.commit ();

			}

		}

		@Override
		public
		void run () {

			while (true) {

				// get the next message

				Long messageId;

				try {

					messageId =
						buffer.next ();

				} catch (InterruptedException interruptedException) {
					return;
				}

				// handle it

				TaskLogger taskLogger =
					logContext.createTaskLogger (
						"run");

				try {

					doMessage (
						taskLogger,
						messageId);

				} catch (Exception exception) {

					doError (
						taskLogger,
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
		Set <Long> activeMessageids =
			buffer.getKeys ();

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ReceivedManager.doQuery ()",
					this);

		) {

			List <InboxRec> inboxes =
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

						Thread.sleep (
							sleepInterval);

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

	@Override
	protected
	boolean checkEnabled () {

		return parseBooleanYesNoRequired (
			mapItemForKeyOrDefault (
				ifNull (
					wbsConfig.runtimeSettings (),
					emptyMap ()),
				"received-manager.enable",
				"yes"));

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

		thread.setName (
			"RecManA");

		thread.start ();

		registerThread (
			thread);

		// create worker threads

		for (int i = 0; i < numWorkerThreads; i ++) {

			thread =
				threadManager.makeThread (
					new ReceivedThread ("" + i));

			thread.setName (
				"RecMan" + i);

			thread.start ();

			registerThread (
				thread);

		}

	}

}
