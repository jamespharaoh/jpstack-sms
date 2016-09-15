package wbs.sms.modempoll.daemon;

import static wbs.utils.string.StringUtils.stringFormat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.gsm.Pdu;
import wbs.sms.gsm.SmsDeliverPdu;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.modempoll.model.ModemPollQueueObjectHelper;
import wbs.sms.modempoll.model.ModemPollQueueRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
public
class ModemPollDaemon
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	ModemPollQueueObjectHelper modemPollQueueHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

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
	void createThreads () {

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

			// open the modem
			try {
				modemIn = new BufferedReader(new InputStreamReader(
						new FileInputStream(deviceName), "us-ascii"));
				modemOut = new OutputStreamWriter(new FileOutputStream(
						deviceName), "us-ascii");
			} catch (IOException e) {
				log.error("Error opening modem device: " + e.getMessage());
				if (modemIn != null)
					try {
						modemIn.close();
					} catch (IOException e1) {
					}
				if (modemOut != null)
					try {
						modemOut.close();
					} catch (IOException e1) {
					}
				modemIn = null;
				modemOut = null;
				return;
			}

			try {

				// outer loop restarts us after an error
				while (true) {

					// poll the modem
					try {

						// do like a decent reset of the modem
						sendCommand("\route\route+++\route\route");

						// initialise modem
						sendCommandOk("at&f0e0+cmgf=0\route");

						// main loop
						while (true) {

							// poll the modem
							doPoll();

							// do our normal sleep
							Thread.sleep(pollSleepTime);
						}

					} catch (IOException e) {

						log.error("Got IO Exception from modem: "
								+ e.getMessage());

						log.error("Going to sleep for a bit");

						Thread.sleep(errorSleepTime);

						continue;

					} catch (ModemException e) {

						log.error("Had problem with modem: "
								+ e.getMessage());

						log.error("Going to sleep for a bit");

						Thread.sleep(errorSleepTime);

						continue;

					}

				}

			} catch (InterruptedException e) {
				return;
			} finally {
				try {
					modemIn.close();
				} catch (IOException e1) {
				}
				try {
					modemOut.close();
				} catch (IOException e1) {
				}
				modemIn = null;
				modemOut = null;
			}
		}

		/**
		 * Polls the modem for each message, calling handlePdu for each one,
		 * then deletes them.
		 */
		private
		void doPoll ()
			throws
				InterruptedException,
				IOException {

			// get the list

			List<String> lines =
				sendCommand (
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
					pduLine);

				// now delete it

				sendCommandOk (
					"at+cmgd=" + matcher.group (1) + "\route");

			}

		}

		/**
		 * Send a command and collect the response.
		 */
		private
		List<String> sendCommand (
				String command)
			throws
				InterruptedException,
				IOException {

			log.debug (
				stringFormat (
					"Sent: %s",
					command.replaceAll (
						"\\route",
						"\\\\route")));

			List<String> ret =
				new ArrayList<String> ();

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

					log.debug (
						stringFormat (
							"Got: " + line));

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

		/**
		 * Send a command and check we get an "OK" back.
		 */
		public
		void sendCommandOk (
				String command)
			throws
				InterruptedException,
				IOException {

			List<String> lines =
				sendCommand (
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

		/**
		 * Send a series of commands with sendCommandOk.
		 */
		@SuppressWarnings ("unused")
		public
		void sendCommandsOk (
				List<String> commands)
			throws
				InterruptedException,
				IOException {

			for (String command : commands)
				sendCommandOk (command);

		}

		/**
		 * Creates and stores a new ModemPollQueue object for this PDU.
		 */
		public
		void storePdu (
				String pdu) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"ModemPollDaemon.RetrieveThread.storePdu (pdu)",
					this);

			modemPollQueueHelper.insert (
				modemPollQueueHelper.createInstance ()

				.setPdu (
					pdu)

			);

		}

	}

	class ProcessThread
		implements Runnable {

		@Override
		public
		void run() {
			try {

				while (true) {

					// process any messages

					processAll ();

					// wait a bit or until the flag is set

					flagWaiter.waitTrue (
						processSleepTime);
				}

			} catch (InterruptedException exception) {

				return;

			}

		}

		void processAll ()
			throws InterruptedException {

			while (true) {

				flagWaiter.clear();

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							"ModemPollDaemon.ProcessThread.processAll ()",
							this);

					ModemPollQueueRec modemPollQueue =
						modemPollQueueHelper.findNext (
							transaction.now ());

					if (modemPollQueue == null)
						return;

					try {

						processOne (
							modemPollQueue);

						modemPollQueueHelper.remove (
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

					transaction.commit();

				} catch (Exception e) {
					log.error("Unhandled exception: " + e.getMessage());
					log.error("Sleeping for a bit...");
					Thread.sleep(5000);
				}
			}
		}

		void processOne (
				ModemPollQueueRec mpq) {

			Pdu pdu =
				decodePduString (
					mpq.getPdu ());

			if (pdu instanceof SmsDeliverPdu) {
				handlePdu((SmsDeliverPdu) pdu);
			} else {
				throw new RuntimeException("Unknown PDU type "
						+ pdu.getClass().getName());
			}

		}

		private
		void handlePdu (
				SmsDeliverPdu pdu) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"ModelPollDaemon.ProcessThread.handlePdu (pdu)",
					this);

			log.info (
				stringFormat (
					"Got message from %s: %s",
					pdu.getOriginatingAddress ().getAddressValue (),
					pdu.getMessage ()));

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			smsInboxLogic.inboxInsert (
				Optional.<String>absent (),
				textHelper.findOrCreate (pdu.getMessage ()),
				pdu.getOriginatingAddress ().getAddressValue (),
				destinationNumber,
				route,
				Optional.<NetworkRec>absent (),
				Optional.<Instant>absent (),
				Collections.<MediaRec>emptyList (),
				Optional.<String>absent (),
				Optional.<String>absent ());

			transaction.commit ();

		}

		/**
		 * Decodes a PDU string including SMSC details and returns the Pdu.
		 */
		public
		Pdu decodePduString (
				String str) {

			try {
				ByteBuffer bb = ByteBuffer.wrap(Pdu.hexToByteArray(str));
				Pdu.skipSmsc(bb);
				return Pdu.decode(bb);
			} catch (RuntimeException e) {
				log.error("PDU string caused exception: " + str);
				throw new RuntimeException (e);
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
