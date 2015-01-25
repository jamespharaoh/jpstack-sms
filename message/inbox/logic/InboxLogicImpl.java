package wbs.sms.message.inbox.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartBufferObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.message.inbox.model.InboxMultipartLogObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartLogRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("inboxLogic")
public
class InboxLogicImpl
	implements InboxLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	InboxMultipartBufferObjectHelper inboxMultipartBufferHelper;

	@Inject
	InboxMultipartLogObjectHelper inboxMultipartLogHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	NumberLogic numberLogic;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	ObjectManager objectManager;

	// implementation

	@Override
	public
	MessageRec inboxInsert (
			String otherId,
			@NonNull TextRec text,
			@NonNull Object numFrom,
			@NonNull String numTo,
			@NonNull RouteRec route,
			NetworkRec network,
			Date networkTime,
			List<MediaRec> medias,
			String avStatus,
			String subject) {

		// sanity check

		if (route == null)
			throw new NullPointerException ("route");

		// lookup basics

		RootRec root =
			rootHelper.find (0);

		// lookup the number

		NumberRec number =
			numberLogic.objectToNumber (numFrom);

		if (network == null) {

			network =
				networkHelper.find (0);

		}

		if (! route.getCanReceive ()) {

			throw new RuntimeException (
				stringFormat (
					"Cannot receive on route %s",
					route.getId ()));

		}

		// see if this otherId already exists and return existing message if so

		MessageRec message =
			otherId != null
				? messageHelper.findByOtherId (
					MessageDirection.in,
					route,
					otherId)
				: null;

		if (message != null) {

			// check the details match

			if (message.getDirection () != MessageDirection.in
					|| message.getText () != text
					|| message.getNumber () != number
					|| ! message.getNumTo ().equals (numTo)
					|| message.getNetwork () != network) {

				log.error ("Trying to insert inbox with duplicated other id, but other details don't match");
				log.error ("Other id: " + otherId);

				log.error ("Existing text: " + message.getText().getText());
				log.error ("Existing num from: " + message.getNumFrom());
				log.error ("Existing num to: " + message.getNumTo());
				log.error ("Existing network: " + message.getNetwork().getId());

				log.error ("New text: " + text.getText());
				log.error ("New num from: " + number.getNumber());
				log.error ("New num to: " + numTo);
				log.error ("New network: " + network.getId());

				throw new RuntimeException (
					"Duplicated other id but message details don't match: "
					+ otherId);

			}

			// and return it

			return message;

		}

		Date now = new Date ();

		// create the message

		AffiliateRec systemAffiliate =
			affiliateHelper.findByCode (
				root,
				"system");

		message =
			new MessageRec ()

			.setCreatedTime (
				now)

			.setDirection (
				MessageDirection.in)

			.setStatus (
				MessageStatus.pending)

			.setOtherId (
				otherId)

			.setText (
				text)

			.setNumber (
				number)

			.setNumFrom (
				number.getNumber ())

			.setNumTo (
				numTo)

			.setCharge (
				route.getInCharge ())

			.setRoute (
				route)

			.setNetwork (
				network)

			.setNetworkTime (
				networkTime)

			.setService (
				serviceHelper.findByCode (
					root,
					"system"))

			.setAffiliate (
				systemAffiliate)

			.setBatch (
				batchHelper.find (0))

			.setAdultVerified (
				avStatus)

			.setMessageType (
				messageTypeHelper.findByCode (
					GlobalId.root,
					medias != null
						? "mms"
						: "sms"))

			.setSubjectText (
				textHelper.findOrCreate (subject));

		if (medias != null)
			for (MediaRec media : medias)
				message.getMedias ().add (media);

		objectManager.insert (
			message);

		// create the inbox entry

		inboxHelper.insert (
			new InboxRec ()
				.setMessage (message));

		log.info (
			stringFormat (
				"SMS %s %s %s %s %s %s",
				message.getId (),
				route.getCode (),
				message.getOtherId (),
				message.getNumFrom (),
				message.getNumTo (),
				message.getText ().getText ()));

		// update the number

		setNetworkFromMessage (
			message);

		// return

		return message;

	}

	@Override
	public
	InboxMultipartBufferRec insertInboxMultipart (
			RouteRec route,
			int multipartId,
			int multipartSegMax,
			int multipartSeg,
			String msgTo,
			String msgFrom,
			Date msgNetworkTime,
			NetworkRec msgNetwork,
			String msgOtherId,
			String msgText) {

		log.info ("MULTI: IN " + multipartSeg);

		try {

			if (! route.getCanReceive ())
				throw new RuntimeException ("Cannot receive on this route");

			// lock route to ensure atomicity

			routeHelper.lock (
				route);

			// if a message part with this otherId has already been received,
			// ignore it

			if (msgOtherId != null) {

				List<InboxMultipartBufferRec> list =
					inboxMultipartBufferHelper.findByOtherId (
						route,
						msgOtherId);

				if (! list.isEmpty ()) {

					log.error (
						"ERROR: inboxMultipartBufferExists");

					return null;

				}

			}

			// create new buffer entry

			InboxMultipartBufferRec inboxMultipartBuffer =
				inboxMultipartBufferHelper.insert (
					new InboxMultipartBufferRec ()

				.setRoute (
					route)

				.setMultipartId (
					multipartId)

				.setMultipartSegMax (
					multipartSegMax)

				.setMultipartSeg (
					multipartSeg)

				.setMsgTo (
					msgTo)

				.setMsgFrom (
					msgFrom)

				.setMsgNetworkTime (
					msgNetworkTime)

				.setMsgNetwork (
					msgNetwork)

				.setMsgOtherId (
					msgOtherId)

				.setMsgText (
					msgText)

			);

			log.info (
				"MULTI: insert " + multipartSeg);

			boolean state =
				insertInboxMultipartMessage (
					inboxMultipartBuffer);

			log.info (
				"MULTI: message insert " + state);

			return inboxMultipartBuffer;

		} finally {

			log.info (
				"MULTI: OUT " + multipartSeg);

		}

	}

	@Override
	public
	boolean insertInboxMultipartMessage (
			InboxMultipartBufferRec inboxMultipartBuffer) {

		// define "recent" as 24 hours ago

		Calendar calendar =
			Calendar.getInstance ();

		calendar.add (
			+ Calendar.HOUR,
			- 36);

		Date recentDate =
			calendar.getTime ();

		log.info (
			"MULTI: recentDate " + recentDate);

		// check if there is a recent log entry, if so just ignore

		List<InboxMultipartLogRec> logList =
			inboxMultipartLogHelper.findRecent (
				inboxMultipartBuffer,
				recentDate);

		if (! logList.isEmpty ()) {

			log.error (
				"ERROR: inboxMultipartLogExistsRecent");

			return false;

		}

		// check for all recent buffer entries

		List<InboxMultipartBufferRec> recentInboxMultipartBuffers =
			inboxMultipartBufferHelper.findRecent (
				inboxMultipartBuffer,
				recentDate);

		// concatenate all the bits, if there are any missing just return

		InboxMultipartBufferRec[] bits =
			new InboxMultipartBufferRec [
				inboxMultipartBuffer.getMultipartSegMax ()];

		for (InboxMultipartBufferRec recentInboxMultipartBuffer
				: recentInboxMultipartBuffers) {

			log.info (
				"MULTI: found " + recentInboxMultipartBuffer.getMultipartSeg ());

			bits [recentInboxMultipartBuffer.getMultipartSeg () - 1] =
				recentInboxMultipartBuffer;

		}

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (int i = 0; i < bits.length; i++) {

			if (bits [i] == null) {
				log.error ("ERROR: InboxMultipartBufferRec " + (i + 1));
				return false;
			}

			stringBuilder.append (bits[i].getMsgText());

		}

		// now create the message

		inboxInsert (
			bits [0].getMsgOtherId (),
			textHelper.findOrCreate (
				stringBuilder.toString ()),
			numberHelper.findOrCreate (
				inboxMultipartBuffer.getMsgFrom ()),
			inboxMultipartBuffer.getMsgTo (),
			inboxMultipartBuffer.getRoute (),
			null,
			null,
			null,
			null,
			null);

		// and create log entry

		inboxMultipartLogHelper.insert (
			new InboxMultipartLogRec ()

			.setRoute (
				inboxMultipartBuffer.getRoute ())

			.setMsgFrom (
				inboxMultipartBuffer.getMsgFrom ())

			.setMultipartId (
				inboxMultipartBuffer.getMultipartId ())

			.setMultipartSegMax (
				inboxMultipartBuffer.getMultipartSegMax ())

		);

		return true;

	}

	@Override
	public
	void inboxProcessed (
			MessageRec message,
			ServiceRec service,
			AffiliateRec affiliate,
			CommandRec command) {

		Transaction transaction =
			database.currentTransaction ();

		InboxRec inbox =
			inboxHelper.find (
				message.getId ());

		// sanity check

		if (message.getStatus () != MessageStatus.pending) {

			throw new RuntimeException (
				stringFormat (
					"Message %d status %s invalid",
					message.getId (),
					message.getStatus ()));

		}

		if (inbox == null)
			throw new RuntimeException ();

		if (inbox.getState () != InboxState.pending)
			throw new RuntimeException ();

		// update inbox

		inbox

			.setState (
				InboxState.processed);

		// update message

		messageLogic.messageStatus (
			message,
			MessageStatus.processed);

		message

			.setProcessedTime (
				instantToDate (
					transaction.now ()))

			.setService (
				ifNull (
					service,
					message.getService ()))

			.setAffiliate (
				ifNull (
					affiliate,
					message.getAffiliate ()))

			.setCommand (
				ifNull (
					command,
					message.getCommand ()));

	}

	@Override
	public
	void inboxNotProcessed (
			MessageRec message,
			ServiceRec service,
			AffiliateRec affiliate,
			CommandRec command,
			String information) {

		log.info (
			stringFormat (
				"Not processed message: %s",
				information));

		Transaction transaction =
			database.currentTransaction ();

		InboxRec inbox =
			inboxHelper.find (
				message.getId ());

		// sanity check

		if (message.getStatus () != MessageStatus.pending) {

			throw new RuntimeException (
				stringFormat (
					"Message %d status %s invalid",
					message.getId (),
					message.getStatus ()));

		}

		if (inbox == null)
			throw new RuntimeException ();

		if (inbox.getState () != InboxState.pending)
			throw new RuntimeException ();

		// update inbox

		inbox

			.setState (
				InboxState.notProcessed);

		// update message

		messageLogic.messageStatus (
			message,
			MessageStatus.notProcessed);

		message

			.setProcessedTime (
				instantToDate (
					transaction.now ()))

			.setService (
				ifNull (
					service,
					message.getService ()))

			.setAffiliate (
				ifNull (
					affiliate,
					message.getAffiliate ()))

			.setCommand (
				ifNull (
					command,
					message.getCommand ()));

	}

	void setNetworkFromMessage (
			MessageRec message) {

		// sanity check

		if (message.getDirection ()
				!= MessageDirection.in)
			throw new RuntimeException ();

		// tools

		NumberRec number =
			message.getNumber ();

		RouteRec route =
			message.getRoute ();

		NetworkRec oldNetwork =
			number.getNetwork ();

		NetworkRec newNetwork =
			message.getNetwork ();

		// ignore if new network unknown

		if (newNetwork.getId () == 0)
			return;

		// ignore if no change

		if (equal (
				oldNetwork,
				newNetwork))
			return;

		// route network behaviour

		switch (route.getNetworkBehaviour ()) {

		case neverUpdate:

			return;

		case updateIfUnknown:

			if (number.getNetwork ().getId () != 0)
				return;

			break;

		case updateKeepingVirtual:

			if (equal (
					oldNetwork.getVirtualNetworkOfNetwork (),
					newNetwork))
				return;

			break;

		case alwaysUpdate:

			break;

		default:

			throw new RuntimeException ();

		}

		// update it

		number.setNetwork (newNetwork);

		// create event

		eventLogic.createEvent (
			"number_network_from_message",
			number,
			oldNetwork,
			newNetwork,
			message);

	}

}
