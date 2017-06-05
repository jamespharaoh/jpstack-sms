package wbs.sms.modempoll.daemon;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.gsm.Pdu;
import wbs.sms.gsm.SmsDeliverPdu;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.modempoll.model.ModemPollQueueObjectHelper;
import wbs.sms.modempoll.model.ModemPollQueueRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

public
class ModemPollDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModemPollQueueObjectHelper modemPollQueueHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// properties

	@Getter @Setter
	String deviceName;

	@Getter @Setter
	Long routeId;

	@Getter @Setter
	String destinationNumber;

	@Getter @Setter
	int pollSleepTime = 2000;

	@Getter @Setter
	int modemSleepTime1 = 100;

	@Getter @Setter
	int modemSleepTime2 = 1500;

	@Getter @Setter
	int errorSleepTime = 10000;

	@Getter @Setter
	int processSleepTime = 5000;

	private final
	FlagWaiter flagWaiter =
		new FlagWaiter ();

	private final static
	Pattern cmglPattern =
		Pattern.compile (
			"\\+CMGL: (\\d*),(\\d*),(\\d*),(\\d*)");

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	void createThreads (
			@NonNull TaskLogger parentTaskLogger) {

		Thread threadA =
			threadManager.makeThread (
				new RetrieveThread (),
				"MdmPollA");

		threadA.start();

		registerThread (threadA);

		Thread threadB =
			threadManager.makeThread (
				new ProcessThread (),
				"MdmPollB");

		threadB.start ();

		registerThread (threadB);

	}

	private final static
	Pattern pduPattern =
		Pattern.compile ("(?:[A-Z0-9]{2})+");

	private
	class RetrieveThread
		implements Runnable {

		BufferedReader modemIn;

		OutputStreamWriter modemOut;

		@Override
		public
		void run () {

			openModem ();

			try {

				// outer loop restarts us after an error

				while (true) {

					TaskLogger taskLogger =
						logContext.createTaskLogger (
							"RetrieveThread.loop");

					// poll the modem

					try {

						// do like a decent reset of the modem

						sendCommand (
							taskLogger,
							"\route\route+++\route\route");

						// initialise modem

						sendCommandOk (
							taskLogger,
							"at&f0e0+cmgf=0\route");

						// main loop

						while (true) {

							// poll the modem

							doPoll (
								taskLogger);

							// do our normal sleep

							Thread.sleep (
								pollSleepTime);

						}

					} catch (IOException e) {

						taskLogger.errorFormat (
							"Got IO Exception from modem: %s",
							e.getMessage ());

						taskLogger.errorFormat (
							"Going to sleep for a bit");

						Thread.sleep (
							errorSleepTime);

						continue;

					} catch (ModemException e) {

						taskLogger.errorFormat (
							"Had problem with modem: %s",
							e.getMessage ());

						taskLogger.errorFormat (
							"Going to sleep for a bit");

						Thread.sleep (
							errorSleepTime);

						continue;

					}

				}

			} catch (InterruptedException e) {

				return;

			} finally {

				try {

					modemIn.close ();

				} catch (IOException e1) {
				}

				try {

					modemOut.close ();

				} catch (IOException e1) {
				}

				modemIn = null;
				modemOut = null;

			}

		}

		private
		void openModem () {

			try (

				OwnedTaskLogger taskLogger =
					logContext.createTaskLogger (
						"RetrieveThread.openModem");

			) {

				try {

					modemIn =
						new BufferedReader (
							new InputStreamReader (
								new FileInputStream (
									deviceName),
									"us-ascii"));

					modemOut =
						new OutputStreamWriter (
							new FileOutputStream (
								deviceName),
							"us-ascii");

				} catch (IOException e) {

					taskLogger.errorFormat (
						"Error opening modem device: %s",
						e.getMessage ());

					if (modemIn != null) {

						try {

							modemIn.close ();

						} catch (IOException e1) {
						}

					}

					if (modemOut != null) {

						try {

							modemOut.close ();

						} catch (IOException e1) {
						}

					}

					modemIn = null;

					modemOut = null;

					return;

				}

			}

		}

		private
		void doPoll (
				@NonNull TaskLogger parentTaskLogger)
			throws
				InterruptedException,
				IOException {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"doPoll");

			) {

				// get the list

				List <String> lines =
					sendCommand (
						taskLogger,
						"at+cmgl=4\route");

				// then go through it

				Iterator<String> iterator =
					lines.iterator ();

				while (iterator.hasNext ()) {

					String line =
						iterator.next ();

					// if its not a +cmgl line skip it

					Matcher matcher =
						cmglPattern.matcher (
							line);

					if (! matcher.matches ())
						continue;

					// ok get the pdu line and decode it

					if (! iterator.hasNext ()) {

						throw new ModemException (
							"Missing PDU data");

					}

					String pduLine =
						iterator.next ();

					if (! pduPattern.matcher (pduLine).matches ()) {

						throw new ModemException (
							"Invalid or missing PDU data");

					}

					// store it

					storePdu (
						taskLogger,
						pduLine);

					// now delete it

					sendCommandOk (
						taskLogger,
						"at+cmgd=" + matcher.group (1) + "\route");

				}

			}

		}

		/**
		 * Send a command and collect the response.
		 */
		private
		List <String> sendCommand (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String command)
			throws
				InterruptedException,
				IOException {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"sendCommand");

			) {

				taskLogger.debugFormat (
					"Sent: %s",
					command.replaceAll (
						"\\route",
						"\\\\route"));

				List <String> ret =
					new ArrayList<> ();

				// send the command

				modemOut.write (
					command);

				modemOut.flush ();

				// give the modem 0.1s to responde
				Thread.sleep(modemSleepTime1);

				for (;;) {

					// read any data and add it to the list

					while (modemIn.ready ()) {

						String line =
							modemIn.readLine ();

						taskLogger.debugFormat (
							"Got: %s",
							line);

						ret.add (
							line);

					}

					// wait a second, if there's no more data then stop

					Thread.sleep (
						modemSleepTime2);

					if (! modemIn.ready ())
						break;

				}

				// and return

				return ret;

			}

		}

		/**
		 * Send a command and check we get an "OK" back.
		 */
		public
		void sendCommandOk (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String command)
			throws
				InterruptedException,
				IOException {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"sendCommandOk");

			) {

				List <String> lines =
					sendCommand (
						taskLogger,
						command);

				for (
					String line
						: lines
				) {

					if (line.equals("OK"))
						return;

				}

				throw new ModemException (
					"Command failed: " + command);

			}

		}

		/**
		 * Send a series of commands with sendCommandOk.
		 */
		@SuppressWarnings ("unused")
		public
		void sendCommandsOk (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull List <String> commands)
			throws
				InterruptedException,
				IOException {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"sendCommandsOk");

			) {

				for (
					String command
						: commands
				) {

					sendCommandOk (
						taskLogger,
						command);

				}

			}

		}

		/**
		 * Creates and stores a new ModemPollQueue object for this PDU.
		 */
		public
		void storePdu (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String pdu) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"storePdu");

			) {

				modemPollQueueHelper.insert (
					transaction,
					modemPollQueueHelper.createInstance ()

					.setPdu (
						pdu)

				);

			}

		}

	}

	class ProcessThread
		implements Runnable {

		@Override
		public
		void run() {

			try {

				while (true) {

					try (

						OwnedTaskLogger taskLogger =
							logContext.createTaskLogger (
								"ProcessThread.run");

					) {

						// process any messages

						processAll (
							taskLogger);

						// wait a bit or until the flag is set

						flagWaiter.waitTrue (
							processSleepTime);

					}

				}

			} catch (InterruptedException exception) {

				return;

			}

		}

		void processAll (
				@NonNull TaskLogger parentTaskLogger)
			throws InterruptedException {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"processAll");

			) {

				while (true) {

					flagWaiter.clear ();

					if (
						! processOne (
							taskLogger)
					) {
						return;
					}

				}

			}

		}

		private
		boolean processOne (
				@NonNull TaskLogger parentTaskLogger)
			throws InterruptedException {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"processOne");

			) {

				try {

					ModemPollQueueRec modemPollQueue =
						modemPollQueueHelper.findNext (
							transaction,
							transaction.now ());

					if (modemPollQueue == null) {
						return false;
					}

					try {

						processOneReal (
							transaction,
							modemPollQueue);

						modemPollQueueHelper.remove (
							transaction,
							modemPollQueue);

					} catch (Exception e) {

						modemPollQueue

							.setRetryTime (
								transaction.now ().plus (
									Duration.standardSeconds (
										modemPollQueue.getTries () * 10)))

							.setTries (
								modemPollQueue.getTries () + 1)

							.setError (
								e.getMessage ());

					}

					transaction.commit ();

				} catch (Exception exception) {

					transaction.errorFormat (
						"Unhandled exception: %s",
						exception.getMessage ());

					transaction.errorFormat (
						"Sleeping for a bit...");

					Thread.sleep (5000);

				}

				return true;

			}

		}

		void processOneReal (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull ModemPollQueueRec mpq) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"processOneReal");

			) {

				Pdu pdu =
					decodePduString (
						taskLogger,
						mpq.getPdu ());

				if (pdu instanceof SmsDeliverPdu) {

					handlePdu (
						taskLogger,
						(SmsDeliverPdu) pdu);

				} else {

					throw new RuntimeException("Unknown PDU type "
							+ pdu.getClass().getName());

				}

			}

		}

		private
		void handlePdu (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull SmsDeliverPdu pdu) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"ProcessThread.handlePdu");

			) {

				transaction.noticeFormat (
					"Got message from %s: %s",
					pdu.getOriginatingAddress ().getAddressValue (),
					pdu.getMessage ());

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						routeId);

				smsInboxLogic.inboxInsert (
					transaction,
					optionalAbsent (),
					textHelper.findOrCreate (
						transaction,
						pdu.getMessage ()),
					smsNumberHelper.findOrCreate (
						transaction,
						pdu.getOriginatingAddress ().getAddressValue ()),
					destinationNumber,
					route,
					optionalAbsent (),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

				transaction.commit ();

			}

		}

		/**
		 * Decodes a PDU string including SMSC details and returns the Pdu.
		 */
		public
		Pdu decodePduString (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String str) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"decodePduString");

			) {

				try {

					ByteBuffer bb =
						ByteBuffer.wrap (
							Pdu.hexToByteArray (
								str));

					Pdu.skipSmsc (
						bb);

					return Pdu.decode (
						bb);

				} catch (RuntimeException e) {

					taskLogger.errorFormat(
						"PDU string caused exception: %s",
						str);

					throw new RuntimeException (e);

				}

			}

		}

	}

}

class ModemException
	extends RuntimeException {

	private static final
	long serialVersionUID =
		-2989877789065599215L;

	ModemException (
			String msg) {

		super (
			msg);

	}
}

class FlagWaiter {

	boolean flag;

	public synchronized
	void set () {

		flag = true;

		notifyAll ();

	}

	public synchronized
	void clear () {

		flag = false;

	}

	public synchronized
	void waitTrue (
			long timeout)
		throws InterruptedException {

		if (flag)
			return;

		wait (timeout);

		return;

	}

}
