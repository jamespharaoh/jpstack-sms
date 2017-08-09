package wbs.sms.message.inbox.logic;

import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("smsInboxLogic")
public
class SmsInboxLogicImplementation
	implements SmsInboxLogic {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	InboxAttemptObjectHelper inboxAttemptHelper;

	@SingletonDependency
	InboxObjectHelper inboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	MessageTypeObjectHelper messageTypeHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	NumberLogic numberLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	MessageRec inboxInsert (
			@NonNull Transaction parentTransaction,
			@NonNull Optional <String> otherId,
			@NonNull TextRec text,
			@NonNull NumberRec number,
			@NonNull String numTo,
			@NonNull RouteRec route,
			@NonNull Optional <NetworkRec> optionalNetwork,
			@NonNull Optional <Instant> networkTime,
			@NonNull List <MediaRec> medias,
			@NonNull Optional <String> avStatus,
			@NonNull Optional <String> subject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"inboxInsert");

		) {

			// lookup basics

			RootRec root =
				rootHelper.findRequired (
					transaction,
					0l);

			// lookup the number

			NetworkRec network =
				optionalNetwork.or (
					networkHelper.findRequired (
						transaction,
						0l));

			if (! route.getCanReceive ()) {

				throw new RuntimeException (
					stringFormat (
						"Cannot receive on route %s",
						integerToDecimalString (
							route.getId ())));

			}

			// see if this otherId already exists and return existing message if so

			if (otherId.isPresent ()) {

				MessageRec existingMessage =
					messageHelper.findByOtherId (
						transaction,
						MessageDirection.in,
						route,
						otherId.get ());

				if (existingMessage != null) {

					// check the details match

					if (
						existingMessage.getDirection () != MessageDirection.in
						|| existingMessage.getText () != text
						|| existingMessage.getNumber () != number
						|| ! existingMessage.getNumTo ().equals (numTo)
						|| existingMessage.getNetwork () != network
					) {

						transaction.errorFormat (
							"Trying to insert inbox with duplicated other id, but other details don't match");

						transaction.errorFormat (
							"Other id: %s",
							optionalOrEmptyString (
								otherId));

						transaction.errorFormat (
							"Existing text: %s",
							existingMessage.getText ().getText ());

						transaction.errorFormat (
							"Existing num from: %s",
							existingMessage.getNumFrom ());

						transaction.errorFormat (
							"Existing num to: %s",
							existingMessage.getNumTo ());

						transaction.errorFormat (
							"Existing network: %s",
							integerToDecimalString (
								existingMessage.getNetwork ().getId ()));

						transaction.errorFormat (
							"New text: %s",
							text.getText ());

						transaction.errorFormat (
							"New num from: %s",
							number.getNumber ());

						transaction.errorFormat (
							"New num to: %s",
							numTo);

						transaction.errorFormat (
							"New network: %s",
							integerToDecimalString (
								network.getId ()));

						throw new RuntimeException (
							stringFormat (
								"Duplicated other id but message details don't ",
								"match: %s",
								otherId.or ("(none)")));

					}

					// and return it

					return existingMessage;

				}

			}

			// create the message

			AffiliateRec systemAffiliate =
				affiliateHelper.findByCodeRequired (
					transaction,
					root,
					"system");

			MessageRec message =
				messageHelper.createInstance ()

				.setCreatedTime (
					transaction.now ())

				.setDirection (
					MessageDirection.in)

				.setStatus (
					MessageStatus.pending)

				.setOtherId (
					otherId.orNull ())

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
					networkTime.isPresent ()
						? networkTime.get ()
						: null)

				.setService (
					serviceHelper.findByCodeRequired (
						transaction,
						root,
						"system"))

				.setAffiliate (
					systemAffiliate)

				.setBatch (
					batchHelper.findRequired (
						transaction,
						0l))

				.setAdultVerified (
					avStatus.orNull ())

				.setMessageType (
					messageTypeHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						medias.isEmpty ()
							? "sms"
							: "mms"))

				.setSubjectText (
					subject.isPresent ()
						? textHelper.findOrCreate (
							transaction,
							subject.get ())
						: null);

			message.getMedias ().addAll (
				medias);

			messageHelper.insert (
				transaction,
				message);

			// create the inbox entry

			inboxHelper.insert (
				transaction,
				inboxHelper.createInstance ()

				.setMessage (
					message)

				.setRoute (
					route)

				.setCreatedTime (
					transaction.now ())

				.setState (
					InboxState.pending)

				.setNextAttempt (
					transaction.now ())

			);

			transaction.noticeFormat (
				"SMS %s %s %s %s %s %s",
				integerToDecimalString (
					message.getId ()),
				route.getCode (),
				emptyStringIfNull (
					message.getOtherId ()),
				message.getNumFrom (),
				message.getNumTo (),
				message.getText ().getText ());

			// update the number

			setNetworkFromMessage (
				transaction,
				message);

			// return

			return message;

		}

	}

	@Override
	public
	InboxAttemptRec inboxProcessed (
			@NonNull Transaction parentTransaction,
			@NonNull InboxRec inbox,
			@NonNull Optional<ServiceRec> service,
			@NonNull Optional<AffiliateRec> affiliate,
			@NonNull CommandRec command) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"inboxProcessed");

		) {

			MessageRec message =
				inbox.getMessage ();

			// sanity check

			checkInboxPending (
				inbox);

			// create inbox attempt

			InboxAttemptRec inboxAttempt =
				inboxAttemptHelper.insert (
					transaction,
					inboxAttemptHelper.createInstance ()

				.setInbox (
					inbox)

				.setIndex (
					inbox.getNumAttempts ())

				.setTimestamp (
					transaction.now ())

				.setResult (
					InboxState.processed)

			);

			// update inbox

			inbox

				.setState (
					InboxState.processed)

				.setNumAttempts (
					inbox.getNumAttempts () + 1)

				.setNextAttempt (
					null)

				.setStatusMessage (
					null);

			// update message

			messageLogic.messageStatus (
				transaction,
				message,
				MessageStatus.processed);

			message

				.setProcessedTime (
					transaction.now ())

				.setService (
					service.or (
						message.getService ()))

				.setAffiliate (
					affiliate.or (
						message.getAffiliate ()))

				.setCommand (
					command);

			// return

			return inboxAttempt;

		}

	}

	@Override
	public
	InboxAttemptRec inboxNotProcessed (
			@NonNull Transaction parentTransaction,
			@NonNull InboxRec inbox,
			@NonNull Optional<ServiceRec> service,
			@NonNull Optional<AffiliateRec> affiliate,
			@NonNull Optional<CommandRec> command,
			@NonNull String statusMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"inboxNotProcessed");

		) {

			transaction.noticeFormat (
				"Not processed message: %s",
				statusMessage);

			// sanity check

			checkInboxPending (
				inbox);

			// create inbox attempt

			InboxAttemptRec inboxAttempt =
				inboxAttemptHelper.insert (
					transaction,
					inboxAttemptHelper.createInstance ()

				.setInbox (
					inbox)

				.setIndex (
					inbox.getNumAttempts ())

				.setTimestamp (
					transaction.now ())

				.setResult (
					InboxState.notProcessed)

				.setStatusMessage (
					statusMessage)

			);

			// update inbox

			inbox

				.setState (
					InboxState.notProcessed)

				.setNumAttempts (
					inbox.getNumAttempts () + 1)

				.setNextAttempt (
					null)

				.setStatusMessage (
					statusMessage);

			// create queue item

			MessageRec message =
				inbox.getMessage ();

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					transaction,
					message.getRoute (),
					"not_processed",
					message.getNumber (),
					message,
					message.getNumFrom (),
					message.getText ().getText ());

			// update message

			messageLogic.messageStatus (
				transaction,
				message,
				MessageStatus.notProcessed);

			message

				.setProcessedTime (
					transaction.now ())

				.setService (
					service.isPresent ()
						? service.get ()
						: message.getService ())

				.setAffiliate (
					affiliate.isPresent ()
						? affiliate.get ()
						: message.getAffiliate ())

				.setCommand (
					command.isPresent ()
						? command.get ()
						: message.getCommand ())

				.setNotProcessedQueueItem (
					queueItem);

			// return

			return inboxAttempt;

		}

	}

	private
	void setNetworkFromMessage (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setNetworkFromMessage");

		) {

			// sanity check

			if (
				message.getDirection ()
					!= MessageDirection.in
			) {
				throw new RuntimeException ();
			}

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

			if (
				referenceEqualWithClass (
					NetworkRec.class,
					oldNetwork,
					newNetwork)
			) {
				return;
			}

			// route network behaviour

			switch (route.getNetworkBehaviour ()) {

			case neverUpdate:

				return;

			case updateIfUnknown:

				if (number.getNetwork ().getId () != 0)
					return;

				break;

			case updateKeepingVirtual:

				if (
					referenceEqualWithClass (
						NetworkRec.class,
						oldNetwork.getVirtualNetworkOfNetwork (),
						newNetwork)
				) {
					return;
				}

				break;

			case alwaysUpdate:

				break;

			default:

				throw new RuntimeException ();

			}

			// update it

			number

				.setNetwork (
					newNetwork);

			// create event

			eventLogic.createEvent (
				transaction,
				"number_network_from_message",
				number,
				oldNetwork,
				newNetwork,
				message);

		}

	}

	@Override
	public
	InboxAttemptRec inboxProcessingFailed (
			@NonNull Transaction parentTransaction,
			@NonNull InboxRec inbox,
			@NonNull String statusMessage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"inboxProcessingFailed");

		) {

			// sanity check

			checkInboxPending (
				inbox);

			// create inbox attempt

			InboxAttemptRec inboxAttempt =
				inboxAttemptHelper.insert (
					transaction,
					inboxAttemptHelper.createInstance ()

				.setInbox (
					inbox)

				.setIndex (
					inbox.getNumAttempts ())

				.setTimestamp (
					transaction.now ())

				.setResult (
					InboxState.pending)

				.setStatusMessage (
					statusMessage)

			);

			// update inbox

			inbox

				.setNumAttempts (
					inbox.getNumAttempts () + 1)

				.setNextAttempt (
					inbox.getNextAttempt ().plus (
						Duration.standardSeconds (
							inbox.getNumAttempts ())))

				.setStatusMessage (
					statusMessage);

			// return

			return inboxAttempt;

		}

	}

	void checkInboxPending (
			InboxRec inbox) {

		// check inbox state

		if (inbox.getState () != InboxState.pending) {

			throw new RuntimeException (
				stringFormat (
					"Unable to process inbox %s ",
					integerToDecimalString (
						inbox.getId ()),
					"in state \"%s\"",
					enumNameSpaces (
						inbox.getState ())));

		}

		// check message status

		MessageRec message =
			inbox.getMessage ();

		if (message.getStatus () != MessageStatus.pending) {

			throw new RuntimeException (
				stringFormat (
					"Unable to process message %s ",
					integerToDecimalString (
						message.getId ()),
					"with status \"%s\"",
					enumNameSpaces (
						message.getStatus ())));

		}

	}

}
