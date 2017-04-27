package wbs.sms.message.inbox.logic;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.inbox.model.InboxMultipartBufferObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.message.inbox.model.InboxMultipartLogObjectHelper;
import wbs.sms.message.inbox.model.InboxMultipartLogRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("smsInboxMultipartLogic")
public
class SmsInboxMultipartLogicImplementation
	implements SmsInboxMultipartLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	InboxMultipartBufferObjectHelper inboxMultipartBufferHelper;

	@SingletonDependency
	InboxMultipartLogObjectHelper inboxMultipartLogHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	InboxMultipartBufferRec insertInboxMultipart (
			@NonNull TaskLogger parentTaskLogger,
			RouteRec route,
			long multipartId,
			long multipartSegMax,
			long multipartSeg,
			String msgTo,
			String msgFrom,
			Instant msgNetworkTime,
			NetworkRec msgNetwork,
			String msgOtherId,
			String msgText) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"insertInboxMultipart");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			taskLogger.noticeFormat (
				"MULTI: IN %s",
				integerToDecimalString (
					multipartSeg));

			try {

				if (! route.getCanReceive ())
					throw new RuntimeException ("Cannot receive on this route");

				// lock route to ensure atomicity

				routeHelper.lock (
					route);

				// if a message part with this otherId has already been received,
				// ignore it

				if (msgOtherId != null) {

					List <InboxMultipartBufferRec> list =
						inboxMultipartBufferHelper.findByOtherId (
							route,
							msgOtherId);

					if (! list.isEmpty ()) {

						taskLogger.errorFormat (
							"ERROR: inboxMultipartBufferExists");

						return null;

					}

				}

				// create new buffer entry

				InboxMultipartBufferRec inboxMultipartBuffer =
					inboxMultipartBufferHelper.insert (
						taskLogger,
						inboxMultipartBufferHelper.createInstance ()

					.setRoute (
						route)

					.setTimestamp (
						transaction.now ())

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

				taskLogger.noticeFormat (
					"MULTI: insert %s",
					integerToDecimalString (
						multipartSeg));

				boolean state =
					insertInboxMultipartMessage (
						taskLogger,
						inboxMultipartBuffer);

				taskLogger.noticeFormat (
					"MULTI: message insert %s",
					booleanToYesNo (
						state));

				return inboxMultipartBuffer;

			} finally {

				taskLogger.noticeFormat (
					"MULTI: OUT %s",
					integerToDecimalString (
						multipartSeg));

			}

		}

	}

	@Override
	public
	boolean insertInboxMultipartMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InboxMultipartBufferRec inboxMultipartBuffer) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"insertInboxMultipartMessage");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			// define "recent" as 1 hour ago

			Instant recentTime =
				Instant.now ()

				.minus (
					Duration.standardHours (1));

			// check if there is a recent log entry, if so just ignore

			List<InboxMultipartLogRec> logList =
				inboxMultipartLogHelper.findRecent (
					inboxMultipartBuffer,
					recentTime);

			if (! logList.isEmpty ()) {

				taskLogger.noticeFormat (
					"ERROR: inboxMultipartLogExistsRecent");

				return false;

			}

			// check for all recent buffer entries

			List<InboxMultipartBufferRec> recentInboxMultipartBuffers =
				inboxMultipartBufferHelper.findRecent (
					inboxMultipartBuffer,
					recentTime);

			// concatenate all the bits, if there are any missing just return

			InboxMultipartBufferRec[] bits =
				new InboxMultipartBufferRec [
					toJavaIntegerRequired (
						inboxMultipartBuffer.getMultipartSegMax ())];

			for (
				InboxMultipartBufferRec recentInboxMultipartBuffer
					: recentInboxMultipartBuffers
			) {

				taskLogger.noticeFormat (
					"MULTI: found %s",
					integerToDecimalString (
						recentInboxMultipartBuffer.getMultipartSeg ()));

				int bitIndex =
					toJavaIntegerRequired (
						recentInboxMultipartBuffer.getMultipartSeg () - 1);

				bits [bitIndex] =
					recentInboxMultipartBuffer;

			}

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (
				int index = 0;
				index < bits.length;
				index ++
			) {

				if (bits [index] == null) {

					taskLogger.errorFormat (
						"ERROR: InboxMultipartBufferRec %s",
						integerToDecimalString (
							index + 1));

					return false;

				}

				stringBuilder.append (bits[index].getMsgText());

			}

			// now create the message

			smsInboxLogic.inboxInsert (
				taskLogger,
				optionalOf (
					bits [0].getMsgOtherId ()),
				textHelper.findOrCreate (
					taskLogger,
					stringBuilder.toString ()),
				numberHelper.findOrCreate (
					taskLogger,
					inboxMultipartBuffer.getMsgFrom ()),
				inboxMultipartBuffer.getMsgTo (),
				inboxMultipartBuffer.getRoute (),
				optionalAbsent (),
				optionalAbsent (),
				emptyList (),
				optionalAbsent (),
				optionalAbsent ());

			// and create log entry

			inboxMultipartLogHelper.insert (
				taskLogger,
				inboxMultipartLogHelper.createInstance ()

				.setRoute (
					inboxMultipartBuffer.getRoute ())

				.setTimestamp (
					transaction.now ())

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

}
