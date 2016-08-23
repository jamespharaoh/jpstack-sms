package wbs.sms.message.wap.logic;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
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
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.route.router.model.RouterRec;

@SingletonComponent ("wapPushLogic")
public
class WapPushLogic
	implements MessageRetrierFactory {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;

	@Inject
	NumberLogic numberLogic;

	@Inject
	OutboxObjectHelper outboxHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	// implementation

	public
	MessageRec wapPushSend (
			Long threadId,
			Object number,
			String numFrom,
			TextRec text,
			TextRec url,
			RouterRec router,
			ServiceRec service,
			BatchRec batch,
			AffiliateRec affiliate,
			DeliveryTypeRec deliveryType,
			Long ref,
			boolean sendNow,
			Set<String> tags,
			NetworkRec network) {

		Transaction transaction =
			database.currentTransaction ();

		number =
			numberLogic.objectToNumber (
				number);

		if (threadId != null && threadId < 0)
			threadId = null;

		if (
			isNull (
				service)
		) {

			service =
				serviceHelper.findRequired (
					0l);

		}

		if (
			isNull (
				affiliate)
		) {

			affiliate =
				affiliateHelper.findRequired (
					0l);

		}

		if (
			isNull (
				batch)
		) {

			batch =
				batchHelper.findRequired (
					0l);

		}

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
					route.getId ()));

		}

		MessageRec message =
			messageHelper.createInstance ()

			.setThreadId (
				threadId)

			.setText (
				textHelper.findOrCreate ("WAP PUSH"))

			.setNumFrom (
				numFrom)

			.setNumTo (
				numberLogic.objectToNumber(number).getNumber())

			.setDirection (
				MessageDirection.out)

			.setStatus (
				sendNow
					? MessageStatus.pending
					: MessageStatus.held)

			.setNumber (
				numberLogic.objectToNumber (number))

			.setRoute (
				route)

			.setService (
				service)

			.setNetwork (
				ifNull (
					network,
					numberLogic.objectToNumber (number).getNetwork ()))

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
			message);

		wapPushMessageHelper.insert (
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
			MessageRec oldMessage,
			RouteRec route,
			TextRec textRec) {

		Transaction transaction =
			database.currentTransaction ();

		WapPushMessageRec oldWapPushMessage =
			wapPushMessageHelper.findRequired (
				oldMessage.getId ());

		if (
			isNull (
				textRec)
		) {

			textRec =
				oldWapPushMessage.getTextText ();

		}

		MessageRec message =
			messageHelper.createInstance ()

			.setThreadId (
				oldMessage.getThreadId ())

			.setText (
				textHelper.findOrCreate ("WAP PUSH"))

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
			message);

		wapPushMessageHelper.insert (
			wapPushMessageHelper.createInstance ()

			.setMessage (
				message)

			.setTextText (
				textRec)

			.setUrlText (
				oldWapPushMessage.getUrlText ())

		);

		outboxHelper.insert (
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
	Map<String,MessageRetrier> getMessageRetriersByMessageTypeCode () {

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
				MessageRec rec,
				RouteRec route,
				TextRec textRec) {

			return wapPushRetry (
				rec,
				route,
				textRec);

		}

	}

}
