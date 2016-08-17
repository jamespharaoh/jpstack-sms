package wbs.sms.message.core.logic;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.NumberUtils.parseLong;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringSplitSimple;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
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

	// dependencies

	@Inject
	NumberLogic numberLogic;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	MessageObjectHelper smsMessageHelper;

	@Inject
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
				messageId);

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

			return parseLong (
					mangledMessageId);

		} else {

			List<String> parts =
				stringSplitSimple (
					mangledMessageId,
					"-");

			if (

				notEqual (
					parts.size (),
					3)

				|| notEqual (
					parts.get (0),
					"test")

				|| notEqual (
					parts.get (1),
					root.getFixturesSeed ())

			) {
				return Optional.absent ();
			}

			return parseLong (
				parts.get (2));

		}

	}

	@Override
	public 
	Optional<MessageRec> findMessageByMangledId (
			@NonNull String mangledMessageId) {

System.out.println ("=== A " + mangledMessageId);

		Optional<Long> messageIdOptional =
			unmangleMessageId (
				mangledMessageId);

System.out.println ("=== B " + messageIdOptional);

		if (
			isNotPresent (
				messageIdOptional)
		) {
System.out.println ("=== C");
			return Optional.absent ();
		}

System.out.println ("=== D");
		return smsMessageHelper.find (
			messageIdOptional.get ());

	}

}
