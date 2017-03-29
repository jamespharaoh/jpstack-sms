package wbs.sms.message.wap.logic;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.core.daemon.MessageRetrier;
import wbs.sms.core.daemon.MessageRetrierFactory;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.delivery.model.DeliveryTypeRec;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.route.router.model.RouterRec;

import wbs.utils.etc.NullUtils;

@SingletonComponent ("wapPushLogic")
public
class WapPushLogic
	implements MessageRetrierFactory {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	MessageTypeObjectHelper messageTypeHelper;

	@SingletonDependency
	NumberLogic numberLogic;

	@SingletonDependency
	OutboxObjectHelper outboxHelper;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	WapPushMessageObjectHelper wapPushMessageHelper;

	// implementation

	public
	MessageRec wapPushSend (
			@NonNull TaskLogger parentTaskLogger,
			Long threadId,
			NumberRec number,
			String numFrom,
			TextRec text,
			TextRec url,
			RouterRec router,
			ServiceRec serviceOrNull,
			BatchRec batchOrNull,
			AffiliateRec affiliateOrNull,
			DeliveryTypeRec deliveryType,
			Long ref,
			boolean sendNow,
			Set <String> tags,
			NetworkRec network) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"wapPushSend");

		Transaction transaction =
			database.currentTransaction ();

		ServiceRec service =
			NullUtils.<ServiceRec> ifNull (
				() -> serviceOrNull,
				() -> serviceHelper.findRequired (0l));

		AffiliateRec affiliate =
			NullUtils.<AffiliateRec> ifNull (
				() -> affiliateOrNull,
				() -> affiliateHelper.findRequired (0l));

		BatchRec batch =
			NullUtils.<BatchRec> ifNull (
				() -> batchOrNull,
				() -> batchHelper.findRequired (0l));

		// check this route can send wap push

		MessageTypeRec wapPushMessageType =
			messageTypeHelper.findByCodeRequired (
				GlobalId.root,
				"wap_push");

		RouteRec route =
			routerLogic.resolveRouter (
				router);

		if (! route.getOutboundMessageTypes ().contains (
				wapPushMessageType)) {

			throw new RuntimeException (
				stringFormat (
					"Cannot send wap push on route %s",
					integerToDecimalString (
						route.getId ())));

		}

		MessageRec message =
			messageHelper.createInstance ()

			.setThreadId (
				threadId)

			.setText (
				textHelper.findOrCreate (
					taskLogger,
					"WAP PUSH"))

			.setNumFrom (
				numFrom)

			.setNumTo (
				number.getNumber ())

			.setDirection (
				MessageDirection.out)

			.setStatus (
				sendNow
					? MessageStatus.pending
					: MessageStatus.held)

			.setNumber (
				number)

			.setRoute (
				route)

			.setService (
				service)

			.setNetwork (
				ifNull (
					network,
					number.getNetwork ()))

			.setBatch (
				batch)

			.setCharge (
				route.getOutCharge ())

			.setAffiliate (
				affiliate)

			.setCreatedTime (
				transaction.now ())

			.setDeliveryType (
				deliveryType)

			.setRef (
				ref)

			.setMessageType (
				wapPushMessageType)

			.setNumAttempts (
				0l);

		if (tags != null) {

			for (String tag : tags)
				message.getTags ().add (tag);

		}

		messageHelper.insert (
			taskLogger,
			message);

		wapPushMessageHelper.insert (
			taskLogger,
			wapPushMessageHelper.createInstance ()

			.setMessage (
				message)

			.setTextText (
				text)

			.setUrlText (
				url)

		);

		if (sendNow) {

			outboxHelper.insert (
				taskLogger,
				outboxHelper.createInstance ()

				.setMessage (
					message)

				.setRoute (
					route)

				.setCreatedTime (
					transaction.now ())

				.setRetryTime (
					transaction.now ())

				.setRemainingTries (
					route.getMaxTries ())

			);

		}

		return message;

	}

	/**
	 * Resends the WAP push associated with the given MessageRetryRec. This
	 * should only be called by the appropriate code and is not a general
	 * purpose function.
	 *
	 * Creates a new MessageRec and associated WapPushMessageRec and OutboxRec
	 * and associates them with the MessageRetryRec.
	 *
	 * @param retry
	 *            the MessageRetryRec which needs retrying
	 */
	public
	MessageRec wapPushRetry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec oldMessage,
			@NonNull RouteRec route,
			TextRec textOrNull) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"wapPushRetry");

		Transaction transaction =
			database.currentTransaction ();

		WapPushMessageRec oldWapPushMessage =
			wapPushMessageHelper.findRequired (
				oldMessage.getId ());

		TextRec text =
			NullUtils.<TextRec> ifNull (
				() -> textOrNull,
				() -> oldWapPushMessage.getTextText ());

		MessageRec message =
			messageHelper.createInstance ()

			.setThreadId (
				oldMessage.getThreadId ())

			.setText (
				textHelper.findOrCreate (
					taskLogger,
					"WAP PUSH"))

			.setNumFrom (
				oldMessage.getNumFrom ())

			.setNumTo (
				oldMessage.getNumTo ())

			.setDirection (
				MessageDirection.out)

			.setStatus (
				MessageStatus.pending)

			.setNumber (
				oldMessage.getNumber ())

			.setRoute (
				route)

			.setService (
				oldMessage.getService ())

			.setNetwork (
				oldMessage.getNetwork ())

			.setBatch (
				oldMessage.getBatch ())

			.setCharge (
				oldMessage.getRoute ().getOutCharge ())

			.setAffiliate (
				oldMessage.getAffiliate ())

			.setCreatedTime (
				transaction.now ())

			.setDeliveryType (
				oldMessage.getDeliveryType ())

			.setRef (
				oldMessage.getRef ())

			.setMessageType (
				messageTypeHelper.findByCodeRequired (
					GlobalId.root,
					"wap_push"))

			.setNumAttempts (
				0l);

		if (oldMessage.getTags () != null) {

			message.getTags ().addAll (
				oldMessage.getTags ());

		}

		messageHelper.insert (
			taskLogger,
			message);

		wapPushMessageHelper.insert (
			taskLogger,
			wapPushMessageHelper.createInstance ()

			.setMessage (
				message)

			.setTextText (
				text)

			.setUrlText (
				oldWapPushMessage.getUrlText ())

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
				message.getRoute ().getMaxTries ())

		);

		return message;

	}

	@Override
	public
	Map <String, MessageRetrier> getMessageRetriersByMessageTypeCode () {

		return ImmutableMap.<String,MessageRetrier>of (
			"wap_push",
			new WapPushRetrier ());

	}

	private
	class WapPushRetrier
		implements MessageRetrier {

		@Override
		public
		MessageRec messageRetry (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull MessageRec rec,
				@NonNull RouteRec route,
				TextRec textRec) {

			return wapPushRetry (
				parentTaskLogger,
				rec,
				route,
				textRec);

		}

	}

}
