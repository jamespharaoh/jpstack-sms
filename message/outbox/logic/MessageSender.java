package wbs.sms.message.outbox.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeRec;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.route.router.model.RouterRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageSender")
public
class MessageSender {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	OutboxObjectHelper outboxHelper;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	Integer threadId;

	@Getter @Setter
	NumberRec number;

	@Getter @Setter
	TextRec messageText;

	@Getter @Setter
	String numFrom;

	@Getter @Setter
	RouteRec route;

	@Getter @Setter
	ServiceRec service;

	@Getter @Setter
	BatchRec batch;

	@Getter @Setter
	AffiliateRec affiliate;

	@Getter @Setter
	DeliveryTypeRec deliveryType;

	@Getter @Setter
	Integer ref;

	@Getter @Setter
	TextRec subjectText;

	@Getter @Setter
	Collection<MediaRec> medias;

	@Getter @Setter
	Boolean sendNow = true;

	@Getter @Setter
	Instant sendTime;

	@Getter @Setter
	Set<String> tags;

	@Getter @Setter
	NetworkRec network;

	@Getter @Setter
	UserRec user;

	// custom setters

	public
	MessageSender messageString (
			String messageString) {

		return messageText (
			textHelper.findOrCreate (
				messageString));

	}

	public
	MessageSender subjectString (
			String subjectString) {

		return subjectText (
			textHelper.findOrCreate (
				subjectString));

	}

	public
	MessageSender deliveryTypeCode (
			String deliveryTypeCode) {

		return deliveryType (
			deliveryTypeHelper.findByCode (
				GlobalId.root,
				deliveryTypeCode));

	}

	public
	MessageSender serviceLookup (
			Record<?> parent,
			String code) {

		return service (
			serviceHelper.findByCode (
				parent,
				code));

	}

	public
	MessageSender routerResolve (
			RouterRec router) {

		return route (
			routerLogic.resolveRouter (
				router));

	}

	// implementation

	public
	MessageRec send () {

		if (affiliate == null) {

			affiliate =
				affiliateHelper.findByCode (
					GlobalId.root,
					"system");

		}

		if (batch == null) {

			batch =
				batchHelper.find (0);

		}

		Instant now =
			new Instant ();

		if (sendNow)
			sendTime = now;

		if (! route.getCanSend ())
			throw new RuntimeException (
				"Cannot send on route " + route.getId ());

		if (network == null)
			network = number.getNetwork ();

		// TODO remove big hacky route mapping

		if (in (network.getCode (),
				"uk_o2",
				"uk_three",
				"uk_tmobile",
				"uk_virgin",
				"uk_vodafone")) {

			RootRec root =
				rootHelper.find (0);

			SliceRec defaultSlice =
				sliceHelper.findByCode (
					root,
					wbsConfig.defaultSlice ());

			if (equal (
					route.getCode (),
					"cutemedia_84232_100")
			) {

				route =
					routeHelper.findByCode (
						defaultSlice,
						"oxygen8_84232_100");

			}

			if (equal (
					route.getCode (),
					"dialogue_89505_500")
			) {

				route =
					routeHelper.findByCode (
						defaultSlice,
						"oxygen8_89505_500");

			}

			if (equal (
					route.getCode (),
					"dialogue_88211_500")) {

				route =
					routeHelper.findByCode (
						defaultSlice,
						"oxygen8_88211_500");

			}

			if (equal (
					route.getCode (),
					"dialogue_85722_500")) {

				route =
					routeHelper.findByCode (
						defaultSlice,
						"oxygen8_85722_500");

			}

		}

		MessageRec message =
			new MessageRec ()

			.setThreadId (
				threadId)

			.setText (
				messageText)

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
				network)

			.setBatch (
				batch)

			.setCharge (
				route.getOutCharge ())

			.setAffiliate (
				affiliate)

			.setCreatedTime (
				now.toDate ())

			.setDate (
				now.toDateTime ().toLocalDate ())

			.setDeliveryType (
				deliveryType)

			.setRef (
				ref)

			.setSubjectText (
				subjectText)

			.setMessageType (
				messageTypeHelper.findByCode (
					GlobalId.root,
					medias != null
						? "mms"
						: "sms"))

			.setUser (
				user)

			.setNumAttempts (
				0);

		if (medias != null) {

			for (MediaRec media
					: medias) {

				message.getMedias ().add (
					media);

			}

		}

		if (tags != null) {

			for (String tag
					: tags) {

				message.getTags ().add (
					tag);

			}

		}

		objectManager.insert (
			message);

		if (sendNow) {

			outboxHelper.insert (
				new OutboxRec ()

				.setMessage (
					message)

				.setRoute (
					route)

				.setCreatedTime (
					now.toDate ())

				.setRetryTime (
					sendTime.toDate ())

				.setRemainingTries (
					route.getMaxTries ()));

		}

		return message;

	}

}
