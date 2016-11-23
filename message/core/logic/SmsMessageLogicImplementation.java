package wbs.sms.message.core.logic;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveThreeElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.CollectionUtils.listThirdElementRequired;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseInteger;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitHyphen;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
			MessageRec message) {

		// TODO hard coded for now

		return message
			.getService ()
			.getParentType ()
			.getCode ()
			.equals ("chat");

	}

	@Override
	public
	void messageStatus (
			MessageRec message,
			MessageStatus newStatus) {

		message.setStatus (newStatus);

		// TODO wtf?
		// store delivery status for number
		// following code shouldn't be here really
		if (isChatMessage (message)) {

			numberLogic.updateDeliveryStatusForNumber (
				message.getNumTo (),
				newStatus);

		}

	}

	@Override
	public
	void blackListMessage (
			MessageRec message) {

		// check message state

		if (message.getStatus () == MessageStatus.pending) {

			// lookup outbox

			OutboxRec smsOutbox =
				smsOutboxHelper.findRequired (
					message.getId ());

			// check message is not being sent

			if (smsOutbox.getSending () != null) {

				throw new RuntimeException (
					"Message is being sent");

			}

			// blacklist message

			messageStatus (
				message,
				MessageStatus.blacklisted);

			// remove outbox

			smsOutboxHelper.remove (
				smsOutbox);

		} else if (message.getStatus () == MessageStatus.held) {

			// blacklist message

			messageStatus (
				message,
				MessageStatus.blacklisted);

		} else {

			throw new RuntimeException (
				"Message is not pending/held");

		}

	}

	@Override
	public
	String mangleMessageId (
			@NonNull Long messageId) {

		RootRec root =
			rootHelper.findRequired (
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

	@Override
	public
	Optional<Long> unmangleMessageId (
			@NonNull String mangledMessageId) {

		RootRec root =
			rootHelper.findRequired (
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
				return Optional.absent ();
			}

			return parseInteger (
				listThirdElementRequired (
					parts));

		}

	}

	@Override
	public
	Optional <MessageRec> findMessageByMangledId (
			@NonNull String mangledMessageId) {

		Optional <Long> messageIdOptional =
			unmangleMessageId (
				mangledMessageId);

		if (
			optionalIsNotPresent (
				messageIdOptional)
		) {
			return Optional.absent ();
		}

		return smsMessageHelper.find (
			messageIdOptional.get ());

	}

}
