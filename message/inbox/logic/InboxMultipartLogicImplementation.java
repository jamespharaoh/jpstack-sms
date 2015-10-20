package wbs.sms.message.inbox.logic;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartBufferObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.message.inbox.model.InboxMultipartLogObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartLogRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("inboxMultipartLogic")
public
class InboxMultipartLogicImplementation
	implements InboxMultipartLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	InboxMultipartBufferObjectHelper inboxMultipartBufferHelper;

	@Inject
	InboxMultipartLogObjectHelper inboxMultipartLogHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

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

		Transaction transaction =
			database.currentTransaction ();

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

				.setTimestamp (
					instantToDate (
						transaction.now ()))

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

		Transaction transaction =
			database.currentTransaction ();

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

		inboxLogic.inboxInsert (
			Optional.of (bits [0].getMsgOtherId ()),
			textHelper.findOrCreate (
				stringBuilder.toString ()),
			numberHelper.findOrCreate (
				inboxMultipartBuffer.getMsgFrom ()),
			inboxMultipartBuffer.getMsgTo (),
			inboxMultipartBuffer.getRoute (),
			Optional.<NetworkRec>absent (),
			Optional.<Instant>absent (),
			Collections.<MediaRec>emptyList (),
			Optional.<String>absent (),
			Optional.<String>absent ());

		// and create log entry

		inboxMultipartLogHelper.insert (
			new InboxMultipartLogRec ()

			.setRoute (
				inboxMultipartBuffer.getRoute ())

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setMsgFrom (
				inboxMultipartBuffer.getMsgFrom ())

			.setMultipartId (
				inboxMultipartBuffer.getMultipartId ())

			.setMultipartSegMax (
				inboxMultipartBuffer.getMultipartSegMax ())

		);

		return true;

	}

}
