package wbs.sms.message.outbox.logic;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.earliest;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.notIn;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageExpiryObjectHelper;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.outbox.model.FailedMessageObjectHelper;
import wbs.sms.message.outbox.model.OutboxDao;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("outboxLogic")
public
class OutboxLogicImplementation
	implements OutboxLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	FailedMessageObjectHelper failedMessageHelper;

	@Inject
	MessageExpiryObjectHelper messageExpiryHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	OutboxDao outboxDao;

	@Inject
	OutboxObjectHelper outboxHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	MessageRec resendMessage (
			MessageRec old,
			RouteRec route,
			TextRec textRec,
			MessageTypeRec messageTypeRec) {

		Transaction transaction =
			database.currentTransaction ();

		if (textRec == null)
			textRec = old.getText ();

		if (messageTypeRec == null)
			messageTypeRec = old.getMessageType ();

		MessageRec message =
			messageHelper.insert (
				messageHelper.createInstance ()

			.setThreadId (
				old.getThreadId ())

			.setText (
				textRec) // old.getText ()

			.setNumFrom (
				old.getNumFrom ())

			.setNumTo (
				old.getNumTo ())

			.setDirection (
				MessageDirection.out)

			.setStatus (
				MessageStatus.pending)

			.setNumber (
				old.getNumber ())

			.setRoute (
				route) // old.getRoute ()

			.setService (
				old.getService ())

			.setNetwork (
				old.getNetwork ())

			.setBatch (
				old.getBatch ())

			.setCharge (
				old.getCharge ())

			.setAffiliate (
				old.getAffiliate ())

			.setCreatedTime (
				instantToDate (
					transaction.now ()))

			.setDeliveryType (
				old.getDeliveryType ())

			.setRef (
				old.getRef ())

			.setSubjectText (
				old.getSubjectText ())

			.setMessageType (
				messageTypeRec)

			.setMedias (
				ImmutableList.copyOf (
					old.getMedias ()))

			.setTags (
				ImmutableSet.copyOf (
					old.getTags ()))

			.setNumAttempts (
				0)

		);

		outboxHelper.insert (
			outboxHelper.createInstance ()

			.setMessage (
				message)

			.setRoute (
				message.getRoute ())

			.setCreatedTime (
				instantToDate (
					transaction.now ()))

			.setRetryTime (
				instantToDate (
					transaction.now ()))

			.setRemainingTries (
				message.getRoute ().getMaxTries ()));

		return message;

	}

	@Override
	public
	void unholdMessage (
			@NonNull MessageRec message) {

		Transaction transaction =
			database.currentTransaction ();

		if (message.getStatus () != MessageStatus.held) {

			throw new RuntimeException (
				stringFormat (
					"Trying to unhold message in state: %s",
					message.getStatus ()));

		}

		messageLogic.messageStatus (
			message,
			MessageStatus.pending);

		outboxHelper.insert (
			outboxHelper.createInstance ()

			.setMessage (
				message)

			.setRoute (
				message.getRoute ())

			.setCreatedTime (
				instantToDate (
					transaction.now ()))

			.setRetryTime (
				instantToDate (
					transaction.now ()))

			.setRemainingTries (
				message.getRoute ().getMaxTries ())

		);

	}

	@Override
	public
	void cancelMessage (
			MessageRec message) {

		// check message state

		if (message.getStatus ()
				== MessageStatus.pending) {

			// lookup outbox

			OutboxRec outbox =
				outboxHelper.find (
					message.getId ());

			// check message is not being sent

			if (outbox.getSending () != null) {

				throw new RuntimeException (
					"Message is being sent");

			}

			// cancel message

			messageLogic.messageStatus (
				message,
				MessageStatus.cancelled);

			// remove outbox

			outboxHelper.remove (
				outbox);

		} else if (
			message.getStatus () == MessageStatus.held
		) {

			// cancel message

			messageLogic.messageStatus (
				message,
				MessageStatus.cancelled);

		} else {

			throw new RuntimeException (
				"Message is not pending/held");

		}

	}

	@Override
	public
	OutboxRec claimNextMessage (
			RouteRec route) {

		Transaction transaction =
			database.currentTransaction ();

		OutboxRec outbox =
			outboxHelper.findNext (
				transaction.now (),
				route);

		if (outbox == null)
			return null;

		outbox

			.setSending (
				instantToDate (
					transaction.now ()))

			.setRemainingTries (
				outbox.getRemainingTries () != null
					? outbox.getRemainingTries () - 1
					: null);

		return outbox;

	}

	@Override
	public
	List<OutboxRec> claimNextMessages (
			RouteRec route,
			int limit) {

		Transaction transaction =
			database.currentTransaction ();

		List<OutboxRec> outboxes =
			outboxDao.findNextLimit (
				transaction.now (),
				route,
				limit);

		for (
			OutboxRec outbox
				: outboxes
		) {

			outbox

				.setSending (
					instantToDate (
						transaction.now ()));

			if (outbox.getRemainingTries () != null) {

				outbox

					.setRemainingTries (
						outbox.getRemainingTries () - 1);

			}

		}

		return outboxes;

	}

	@Override
	public
	void messageSuccess (
			int messageId,
			String[] otherIds) {

		Transaction transaction =
			database.currentTransaction ();

		log.debug ("outbox success id = " + messageId);

		OutboxRec outbox =
			outboxHelper.find (
				messageId);

		MessageRec message =
			outbox.getMessage ();

		if (
			message.getStatus () != MessageStatus.pending
			&& message.getStatus () != MessageStatus.cancelled
		) {

			throw new RuntimeException (
				stringFormat (
					"Invalid message status %s for message %d",
					message.getStatus ().toString (),
					message.getId ()));

		}

		if (outbox.getSending () == null)
			throw new RuntimeException (
				"Outbox not marked as sending!");

		outboxHelper.remove (
			outbox);

		messageLogic.messageStatus (
			message,
			MessageStatus.sent);

		if (otherIds != null)
			message.setOtherId (otherIds [0]);

		message

			.setProcessedTime (
				instantToDate (
					transaction.now ()));

		// create expiry if appropriate

		RouteRec route =
			message.getRoute ();

		if (
			route.getDeliveryReports ()
			&& route.getExpirySecs () != null
		) {

			messageExpiryHelper.insert (
				messageExpiryHelper.createInstance ()

				.setMessage (
					message)

				.setExpiryTime (
					Instant.now ()
						.toDateTime ()
						.plusSeconds (route.getExpirySecs ())
						.toInstant ()
						.toDate ()));

		}

		database.flush ();

		// create multipart companions if appropriate

		if (otherIds != null) {

			for (
				int index = 1;
				index < otherIds.length;
				index ++
			) {

				messageHelper.insert (
					messageHelper.createInstance ()

					.setThreadId (
						message.getThreadId ())

					.setOtherId (
						otherIds [index])

					.setText (
						textHelper.findOrCreate (
							stringFormat (
								"[multipart companion for %s]",
								message.getId ())))

					.setNumFrom (
						message.getNumFrom ())

					.setNumTo (
						message.getNumTo ())

					.setDirection (
						MessageDirection.out)

					.setNumber (
						message.getNumber ())

					.setCharge (
						message.getCharge ())

					.setMessageType (
						message.getMessageType ())

					.setRoute (
						message.getRoute ())

					.setService (
						message.getService ())

					.setNetwork (
						message.getNetwork ())

					.setBatch (
						message.getBatch ())

					.setAffiliate (
						message.getAffiliate ())

					.setStatus (
						MessageStatus.sent)

					.setCreatedTime (
						message.getCreatedTime ())

					.setProcessedTime (
						message.getProcessedTime ())

					.setNetworkTime (
						null)

				);

				// TODO expire multipart companions?

			}

		}

	}

	@Override
	public
	void messageFailure (
			int messageId,
			String error,
			@NonNull FailureType failureType) {

		Transaction transaction =
			database.currentTransaction ();

		log.debug (
			"outbox failure id = " + messageId);

		OutboxRec outbox =
			outboxHelper.find (
				messageId);

		MessageRec message =
			outbox.getMessage ();

		if (
			notIn (
				message.getStatus (),
				MessageStatus.pending,
				MessageStatus.cancelled)
		) {

			throw new RuntimeException (
				"Invalid message status");

		}

		if (outbox.getSending () == null) {

			throw new RuntimeException (
				"Outbox not marked as sending!");

		}

		if (failureType == FailureType.perm) {

			outboxHelper.remove (
				outbox);

			messageLogic.messageStatus (
				message,
				MessageStatus.failed);

			message

				.setProcessedTime (
					instantToDate (
						transaction.now ()));

			failedMessageHelper.insert (
				failedMessageHelper.createInstance ()

				.setMessage (
					message)

				.setError (
					error)

			);

		} else {

			Calendar calendar =
				Calendar.getInstance ();

			calendar.add (
				Calendar.SECOND,
				10 * outbox.getTries ());

			outbox

				.setRetryTime (
					calendar.getTime ())

				.setTries (
					outbox.getTries () + 1)

				.setDailyFailure (
					failureType == FailureType.daily)

				.setError (
					error)

				.setSending (
					null);

		}

	}

	@Override
	public
	void retryMessage (
			@NonNull MessageRec message) {

		Transaction transaction =
			database.currentTransaction ();

		if (
			notEqual (
				message.getDirection (),
				MessageDirection.out)
		) {
		
			throw new RuntimeException ();

		} else if (
			in (message.getStatus (),
				MessageStatus.failed)
		) {

			outboxHelper.insert (
				outboxHelper.createInstance ()

				.setMessage (
					message)
	
				.setRoute (
					message.getRoute ())
	
				.setCreatedTime (
					instantToDate (
						transaction.now ()))
	
				.setRetryTime (
					instantToDate (
						transaction.now ()))
	
				.setRemainingTries (
					message.getRoute ().getMaxTries ())

			);

			messageLogic.messageStatus (
				message,
				MessageStatus.pending);

		} else if (
			in (message.getStatus (),
				MessageStatus.pending)
		) {

			OutboxRec existingOutbox =
				outboxHelper.find (
					message);

			existingOutbox
	
				.setRetryTime (
					instantToDate (
						earliest (
							dateToInstant (
								existingOutbox.getRetryTime ()),
							transaction.now ())))
	
				.setRemainingTries (
					existingOutbox.getRemainingTries () != null
						? Math.max (
							existingOutbox.getRemainingTries (),
							1)
						: null);

		} else {

			throw new RuntimeException ();

		}

	}

}
