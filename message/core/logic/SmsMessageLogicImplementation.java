package wbs.sms.message.core.logic;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveThreeElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.CollectionUtils.listThirdElementRequired;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseInteger;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitHyphen;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.number.core.logic.NumberLogic;

@SingletonComponent ("messageLogic")
public
class SmsMessageLogicImplementation
	implements SmsMessageLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberLogic numberLogic;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	@SingletonDependency
	OutboxObjectHelper smsOutboxHelper;

	// implementation

	@Override
	public
	boolean isChatMessage (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"isChatMessage");

		) {

			// TODO hard coded for now

			return message
				.getService ()
				.getParentType ()
				.getCode ()
				.equals ("chat");

		}

	}

	@Override
	public
	void messageStatus (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message,
			@NonNull MessageStatus newStatus) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"messageStatus");

		) {

			message.setStatus (
				newStatus);

			// TODO wtf?
			// store delivery status for number
			// following code shouldn't be here really

			if (
				isChatMessage (
					transaction,
					message)
			) {

				numberLogic.updateDeliveryStatusForNumber (
					transaction,
					message.getNumTo (),
					newStatus);

			}

		}

	}

	@Override
	public
	void blackListMessage (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"blackListMessage");

		) {

			// check message state

			if (
				enumEqualSafe (
					message.getStatus (),
					MessageStatus.pending)
			) {

				// lookup outbox

				OutboxRec smsOutbox =
					smsOutboxHelper.findRequired (
						transaction,
						message.getId ());

				// check message is not being sent

				if (smsOutbox.getSending () != null) {

					throw new RuntimeException (
						"Message is being sent");

				}

				// blacklist message

				messageStatus (
					transaction,
					message,
					MessageStatus.blacklisted);

				// remove outbox

				smsOutboxHelper.remove (
					transaction,
					smsOutbox);

			} else if (message.getStatus () == MessageStatus.held) {

				// blacklist message

				messageStatus (
					transaction,
					message,
					MessageStatus.blacklisted);

			} else {

				throw new RuntimeException (
					"Message is not pending/held");

			}

		}

	}

	@Override
	public
	String mangleMessageId (
			@NonNull Transaction parentTransaction,
			@NonNull Long messageId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"mangleMessageId");

		) {

			RootRec root =
				rootHelper.findRequired (
					transaction,
					0l);

			if (
				isNull (
					root.getFixturesSeed ())
			) {

				return Long.toString (
					messageId);

			} else {

				return stringFormat (
					"test-%s-%s",
					root.getFixturesSeed (),
					integerToDecimalString (
						messageId));

			}

		}

	}

	@Override
	public
	Optional <Long> unmangleMessageId (
			@NonNull Transaction parentTransaction,
			@NonNull String mangledMessageId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"unmangleMessageId");

		) {

			RootRec root =
				rootHelper.findRequired (
					transaction,
					0l);

			if (
				isNull (
					root.getFixturesSeed ())
			) {

				return parseInteger (
						mangledMessageId);

			} else {

				List <String> parts =
					stringSplitHyphen (
						mangledMessageId);

				if (

					collectionDoesNotHaveThreeElements (
						parts)

					|| stringNotEqualSafe (
						listFirstElementRequired (
							parts),
						"test")

					|| stringNotEqualSafe (
						listSecondElementRequired (
							parts),
						root.getFixturesSeed ())

				) {
					return optionalAbsent ();
				}

				return parseInteger (
					listThirdElementRequired (
						parts));

			}

		}

	}

	@Override
	public
	Optional <MessageRec> findMessageByMangledId (
			@NonNull Transaction parentTransaction,
			@NonNull String mangledMessageId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMessageByMangledId");

		) {

			Optional <Long> messageIdOptional =
				unmangleMessageId (
					transaction,
					mangledMessageId);

			if (
				optionalIsNotPresent (
					messageIdOptional)
			) {
				return optionalAbsent ();
			}

			return smsMessageHelper.find (
				transaction,
				messageIdOptional.get ());

		}

	}

}
