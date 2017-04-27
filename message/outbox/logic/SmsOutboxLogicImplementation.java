package wbs.sms.message.outbox.logic;

import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanOne;
import static wbs.utils.etc.NumberUtils.notMoreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earliest;

import java.util.List;
import java.util.stream.LongStream;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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
import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.logic.SmsMessageLogic;
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
import wbs.sms.message.outbox.model.SmsOutboxAttemptObjectHelper;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptState;
import wbs.sms.message.outbox.model.SmsOutboxMultipartLinkObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("smsOutboxLogic")
public
class SmsOutboxLogicImplementation
	implements SmsOutboxLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FailedMessageObjectHelper failedMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageExpiryObjectHelper messageExpiryHelper;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	OutboxDao outboxDao;

	@SingletonDependency
	OutboxObjectHelper outboxHelper;

	@SingletonDependency
	SmsOutboxAttemptObjectHelper smsOutboxAttemptHelper;

	@SingletonDependency
	SmsOutboxMultipartLinkObjectHelper smsOutboxMultipartLinkHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	MessageRec resendMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec originalMessage,
			@NonNull RouteRec newRoute,
			@NonNull Optional <TextRec> newTextOptional,
			@NonNull Optional <MessageTypeRec> newMessageTypeOptional) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"resendMessage");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			TextRec textRec =
				optionalOr (
					newTextOptional,
					originalMessage.getText ());

			MessageTypeRec messageType =
				optionalOr (
					newMessageTypeOptional,
					originalMessage.getMessageType ());

			MessageRec message =
				messageHelper.insert (
					taskLogger,
					messageHelper.createInstance ()

				.setThreadId (
					originalMessage.getThreadId ())

				.setText (
					textRec) // old.getText ()

				.setNumFrom (
					originalMessage.getNumFrom ())

				.setNumTo (
					originalMessage.getNumTo ())

				.setDirection (
					MessageDirection.out)

				.setStatus (
					MessageStatus.pending)

				.setNumber (
					originalMessage.getNumber ())

				.setRoute (
					newRoute) // old.getRoute ()

				.setService (
					originalMessage.getService ())

				.setNetwork (
					originalMessage.getNetwork ())

				.setBatch (
					originalMessage.getBatch ())

				.setCharge (
					originalMessage.getCharge ())

				.setAffiliate (
					originalMessage.getAffiliate ())

				.setCreatedTime (
					transaction.now ())

				.setDeliveryType (
					originalMessage.getDeliveryType ())

				.setRef (
					originalMessage.getRef ())

				.setSubjectText (
					originalMessage.getSubjectText ())

				.setMessageType (
					messageType)

				.setMedias (
					ImmutableList.copyOf (
						originalMessage.getMedias ()))

				.setTags (
					ImmutableSet.copyOf (
						originalMessage.getTags ()))

				.setNumAttempts (
					0l)

			);

			outboxHelper.insert (
				taskLogger,
				outboxHelper.createInstance ()

				.setMessage (
					message)

				.setRoute (
					message.getRoute ())

				.setCreatedTime (
					transaction.now ())

				.setRetryTime (
					transaction.now ())

				.setRemainingTries (
					message.getRoute ().getMaxTries ()));

			return message;

		}

	}

	@Override
	public
	void unholdMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"unholdMessage");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			if (message.getStatus () != MessageStatus.held) {

				throw new RuntimeException (
					stringFormat (
						"Trying to unhold message in state: %s",
						enumNameSpaces (
							message.getStatus ())));

			}

			messageLogic.messageStatus (
				taskLogger,
				message,
				MessageStatus.pending);

			outboxHelper.insert (
				taskLogger,
				outboxHelper.createInstance ()

				.setMessage (
					message)

				.setRoute (
					message.getRoute ())

				.setCreatedTime (
					transaction.now ())

				.setRetryTime (
					transaction.now ())

				.setRemainingTries (
					message.getRoute ().getMaxTries ())

			);

		}

	}

	@Override
	public
	void cancelMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"cancelMessage");

		) {

			// check message state

			if (
				enumEqualSafe (
					message.getStatus (),
					MessageStatus.pending)
			) {

				// lookup outbox

				OutboxRec outbox =
					outboxHelper.findRequired (
						message.getId ());

				// check message is not being sent

				if (outbox.getSending () != null) {

					throw new RuntimeException (
						"Message is being sent");

				}

				// cancel message

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.cancelled);

				// remove outbox

				outboxHelper.remove (
					outbox);

			} else if (
				enumEqualSafe (
					message.getStatus (),
					MessageStatus.held)
			) {

				// cancel message

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.cancelled);

			} else {

				throw new RuntimeException (
					"Message is not pending/held");

			}

		}

	}

	@Override
	public
	OutboxRec claimNextMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RouteRec route) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"claimNextMessage");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			OutboxRec outbox =
				outboxHelper.findNext (
					transaction.now (),
					route);

			if (outbox == null)
				return null;

			outbox

				.setSending (
					transaction.now ())

				.setRemainingTries (
					outbox.getRemainingTries () != null
						? outbox.getRemainingTries () - 1
						: null);

			return outbox;

		}

	}

	@Override
	public
	List <OutboxRec> claimNextMessages (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RouteRec route,
			@NonNull Long limit) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"claimNextMessages");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			List <OutboxRec> outboxes =
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
						transaction.now ());

				if (outbox.getRemainingTries () != null) {

					outbox

						.setRemainingTries (
							outbox.getRemainingTries () - 1);

				}

			}

			return outboxes;

		}

	}

	@Override
	public
	void messageSuccess (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message,
			@NonNull Optional <List <String>> otherIds,
			@NonNull Optional <Long> simulateMultipart) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"messageSuccess");

		) {

			sanityCheckMultipartOptions (
				otherIds,
				simulateMultipart);

			BorrowedTransaction transaction =
				database.currentTransaction ();

			OutboxRec outbox =
				outboxHelper.findRequired (
					message.getId ());

			// check message state

			if (

				enumNotEqualSafe (
					message.getStatus (),
					MessageStatus.pending)

				&& enumNotEqualSafe (
					message.getStatus (),
					MessageStatus.cancelled)

			) {

				throw new RuntimeException (
					stringFormat (
						"Invalid message status %s for message %s",
						message.getStatus ().toString (),
						integerToDecimalString (
							message.getId ())));

			}

			if (outbox.getSending () == null) {

				throw new RuntimeException (
					"Outbox not marked as sending!");

			}

			// remove the outbox and log success

			outboxHelper.remove (
				outbox);

			messageLogic.messageStatus (
				taskLogger,
				message,
				MessageStatus.sent);

			if (
				optionalIsPresent (
					otherIds)
			) {

				message

					.setOtherId (
						otherIds.get ().get (0));

			}

			message

				.setProcessedTime (
					transaction.now ());

			// create expiry if appropriate

			RouteRec route =
				message.getRoute ();

			if (
				route.getDeliveryReports ()
				&& route.getExpirySecs () != null
			) {

				messageExpiryHelper.insert (
					taskLogger,
					messageExpiryHelper.createInstance ()

					.setMessage (
						message)

					.setExpiryTime (
						Instant.now ().plus (
							Duration.standardSeconds (
								route.getExpirySecs ())))

				);

			}

			database.flush ();

			// create multipart companions from other ids

			if (

				optionalIsPresent (
					otherIds)

				&& collectionHasMoreThanOneElement (
					otherIds.get ())

			) {

				otherIds.get ().stream ().skip (1).forEach (
					otherId -> {

					MessageRec companionMessage =
						messageHelper.insert (
							taskLogger,
							messageHelper.createInstance ()

						.setThreadId (
							message.getThreadId ())

						.setOtherId (
							otherId)

						.setText (
							textHelper.findOrCreateFormat (
								taskLogger,
								"[multipart companion for %s]",
								integerToDecimalString (
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

						.setUser (
							message.getUser ())

					);

					smsOutboxMultipartLinkHelper.insert (
						taskLogger,
						smsOutboxMultipartLinkHelper.createInstance ()

						.setMessage (
							companionMessage)

						.setMainMessage (
							message)

						.setSimulated (
							false)

					);

				});

			}

			// create simulated multipart companions

			if (

				optionalIsPresent (
					simulateMultipart)

				&& moreThanOne (
					simulateMultipart.get ())

			) {

				LongStream.range (1, simulateMultipart.get ()).forEach (
					companionIndex -> {

					MessageRec companionMessage =
						messageHelper.insert (
							taskLogger,
							messageHelper.createInstance ()

						.setThreadId (
							message.getThreadId ())

						.setText (
							textHelper.findOrCreateFormat (
								taskLogger,
								"[multipart companion for %s]",
								integerToDecimalString (
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

						.setUser (
							message.getUser ())

					);

					smsOutboxMultipartLinkHelper.insert (
						taskLogger,
						smsOutboxMultipartLinkHelper.createInstance ()

						.setMessage (
							companionMessage)

						.setMainMessage (
							message)

						.setSimulated (
							true)

					);

				});

			}

		}

	}

	@Override
	public
	void messageFailure (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message,
			@NonNull String error,
			@NonNull FailureType failureType) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"messageFailure");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			taskLogger.debugFormat (
				"outbox failure id = %s",
				integerToDecimalString (
					message.getId ()));

			OutboxRec outbox =
				outboxHelper.findRequired (
					message.getId ());

			if (
				enumNotInSafe (
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

			if (failureType == FailureType.permanent) {

				outboxHelper.remove (
					outbox);

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.failed);

				message

					.setProcessedTime (
						transaction.now ());

				failedMessageHelper.insert (
					taskLogger,
					failedMessageHelper.createInstance ()

					.setMessage (
						message)

					.setError (
						error)

				);

			} else {

				outbox

					.setRetryTime (
						transaction.now ().plus (
							Duration.standardSeconds (
								outbox.getTries () * 10)))

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

	}

	@Override
	public
	void retryMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"retryMessage");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			if (
				enumNotEqualSafe (
					message.getDirection (),
					MessageDirection.out)
			) {

				throw new RuntimeException ();

			} else if (
				enumInSafe (
					message.getStatus (),
					MessageStatus.failed,
					MessageStatus.cancelled,
					MessageStatus.blacklisted)
			) {

				outboxHelper.insert (
					taskLogger,
					outboxHelper.createInstance ()

					.setMessage (
						message)

					.setRoute (
						message.getRoute ())

					.setCreatedTime (
						transaction.now ())

					.setRetryTime (
						transaction.now ())

					.setRemainingTries (
						message.getRoute ().getMaxTries ())

				);

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.pending);

			} else if (
				enumInSafe (
					message.getStatus (),
					MessageStatus.pending)
			) {

				OutboxRec existingOutbox =
					outboxHelper.find (
						message);

				existingOutbox

					.setRetryTime (
						earliest (
							existingOutbox.getRetryTime (),
							transaction.now ()))

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

	@Override
	public
	SmsOutboxAttemptRec beginSendAttempt (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OutboxRec smsOutbox,
			@NonNull Optional<byte[]> requestTrace) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beginSendAttempt");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			MessageRec smsMessage =
				smsOutbox.getMessage ();

			SmsOutboxAttemptRec smsOutboxAttempt =
				smsOutboxAttemptHelper.insert (
					taskLogger,
					smsOutboxAttemptHelper.createInstance ()

				.setMessage (
					smsMessage)

				.setIndex (
					smsMessage.getNumAttempts ())

				.setState (
					SmsOutboxAttemptState.sending)

				.setStatusMessage (
					"Sending")

				.setRoute (
					smsOutbox.getRoute ())

				.setStartTime (
					transaction.now ())

				.setRequestTrace (
					optionalOrNull (
						requestTrace))

			);

			smsMessage

				.setNumAttempts (
					smsMessage.getNumAttempts () + 1);

			return smsOutboxAttempt;

		}

	}

	@Override
	public
	void completeSendAttemptSuccess (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsOutboxAttemptRec smsOutboxAttempt,
			@NonNull Optional <List <String>> otherIds,
			@NonNull Optional <Long> simulateMultipart,
			@NonNull Optional <byte[]> requestTrace,
			@NonNull Optional <byte[]> responseTrace) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"completeSendAttemptSuccess");

		) {

			sanityCheckMultipartOptions (
				otherIds,
				simulateMultipart);

			// create sms outbox attempt

			BorrowedTransaction transaction =
				database.currentTransaction ();

			MessageRec smsMessage =
				smsOutboxAttempt.getMessage ();

			smsOutboxAttempt

				.setState (
					SmsOutboxAttemptState.success)

				.setStatusMessage (
					"Success")

				.setEndTime (
					transaction.now ())

				.setRequestTrace (
					ifNull (
						requestTrace.orNull (),
						smsOutboxAttempt.getRequestTrace ()))

				.setResponseTrace (
					optionalOrNull (
						responseTrace));

			messageSuccess (
				taskLogger,
				smsMessage,
				otherIds,
				simulateMultipart);

		}

	}

	@Override
	public
	void completeSendAttemptFailure (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsOutboxAttemptRec smsOutboxAttempt,
			@NonNull FailureType failureType,
			@NonNull String errorMessage,
			@NonNull Optional <byte[]> requestTrace,
			@NonNull Optional <byte[]> responseTrace,
			@NonNull Optional <byte[]> errorTrace) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"completeSendAttemptFailure");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			MessageRec smsMessage =
				smsOutboxAttempt.getMessage ();

			smsOutboxAttempt

				.setState (
					SmsOutboxAttemptState.failure)

				.setStatusMessage (
					errorMessage)

				.setEndTime (
					transaction.now ())

				.setRequestTrace (
					ifNull (
						requestTrace.orNull (),
						smsOutboxAttempt.getRequestTrace ()))

				.setResponseTrace (
					responseTrace.orNull ())

				.setErrorTrace (
					errorTrace.orNull ());

			messageFailure (
				taskLogger,
				smsMessage,
				errorMessage,
				failureType);

		}

	}

	private
	void sanityCheckMultipartOptions (
			@NonNull Optional <List <String>> otherIds,
			@NonNull Optional <Long> simulateMultipart) {

		// sanity check

		if (
			optionalIsPresent (
				simulateMultipart)
		) {

			// check simulate multipart is one or more

			if (
				notMoreThanZero (
					simulateMultipart.get ())
			) {

				throw new IllegalArgumentException (
					"simulateMultiparts must be greater than zero");

			}

			// check multiple other ids weren't returned

			if (

				optionalIsPresent (
					otherIds)

				&& collectionHasMoreThanOneElement (
					otherIds.get ())

			) {

				throw new IllegalArgumentException (
					stringFormat (
						"simulateMultiparts can only be used with a single ",
						"otherId, but %s were provided",
						integerToDecimalString (
							collectionSize (
								otherIds.get ()))));

			}

		}

	}

}
