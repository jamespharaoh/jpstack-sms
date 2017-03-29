package wbs.sms.message.outbox.logic;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringInSafe;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.delivery.model.DeliveryTypeRec;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.route.router.model.RouterRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageSender")
public
class SmsMessageSender {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	MessageTypeObjectHelper messageTypeHelper;

	@SingletonDependency
	OutboxObjectHelper outboxHelper;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	Long threadId;

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
	Optional<DeliveryTypeRec> deliveryType =
		Optional.<DeliveryTypeRec>absent ();

	@Getter @Setter
	Long ref;

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
	SmsMessageSender deliveryTypeCode (
			@NonNull String deliveryTypeCode) {

		return deliveryTypeCode (
			Optional.of (
				deliveryTypeCode));

	}

	public
	SmsMessageSender messageString (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String messageString) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"messageString");

		return messageText (
			textHelper.findOrCreate (
				taskLogger,
				messageString));

	}

	public
	SmsMessageSender subjectString (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <String> subjectString) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"subjectString");

		return subjectText (
			optionalOrNull (
				textHelper.findOrCreate (
					taskLogger,
					subjectString)));

	}

	public
	SmsMessageSender subjectString (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String subjectString) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"subjectString");

		return subjectText (
			textHelper.findOrCreate (
				taskLogger,
				subjectString));

	}

	public
	SmsMessageSender deliveryTypeCode (
			@NonNull Optional<String> deliveryTypeCode) {

		return deliveryType (
			deliveryTypeCode.isPresent ()
				? Optional.of (
					deliveryTypeHelper.findByCodeRequired (
						GlobalId.root,
						deliveryTypeCode.get ()))
				: Optional.<DeliveryTypeRec>absent ());

	}

	public
	SmsMessageSender serviceLookup (
			@NonNull Record<?> parent,
			@NonNull String code) {

		return service (
			serviceHelper.findByCodeRequired (
				parent,
				code));

	}

	public
	SmsMessageSender routerResolve (
			@NonNull RouterRec router) {

		return route (
			routerLogic.resolveRouter (
				router));

	}

	// implementation

	public
	MessageRec send (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"send");

		Transaction transaction =
			database.currentTransaction ();

		if (affiliate == null) {

			affiliate =
				affiliateHelper.findByCodeRequired (
					GlobalId.root,
					"system");

		}

		if (batch == null) {

			batch =
				batchHelper.findRequired (
					0l);

		}

		if (sendNow) {

			sendTime =
				transaction.now ();

		}

		if (! route.getCanSend ()) {

			throw new RuntimeException (
				"Cannot send on route " + route.getId ());

		}

		if (network == null)
			network = number.getNetwork ();

		// TODO remove big hacky route mapping

		if (
			stringInSafe (
				network.getCode (),
				"uk_o2",
				"uk_three",
				"uk_tmobile",
				"uk_virgin",
				"uk_vodafone")
		) {

			if (
				stringEqualSafe (
					route.getCode (),
					"cutemedia_84232_100")
			) {

				throw new RuntimeException (
					"Hack to update route to oxygen8_84232_100 removed");

				/*
				route =
					routeHelper.findByCodeOrNull (
						defaultSlice,
						"oxygen8_84232_100");
				*/

			}

			if (
				stringEqualSafe (
					route.getCode (),
					"dialogue_89505_500")
			) {

				throw new RuntimeException (
					"Hack to update route to oxygen8_89505_500 removed");

				/*
				route =
					routeHelper.findByCodeOrNull (
						defaultSlice,
						"oxygen8_89505_500");
				*/

			}

			if (
				stringEqualSafe (
					route.getCode (),
					"dialogue_88211_500")
			) {

				throw new RuntimeException (
					"Hack to update route to oxygen8_88211_500 removed");

				/*
				route =
					routeHelper.findByCodeOrNull (
						defaultSlice,
						"oxygen8_88211_500");
				*/

			}

			if (
				stringEqualSafe (
					route.getCode (),
					"dialogue_85722_500")
			) {

				throw new RuntimeException (
					"Hack to update route to oxygen8_85722_500 removed");

				/*
				route =
					routeHelper.findByCodeOrNull (
						defaultSlice,
						"oxygen8_85722_500");
				*/

			}

		}

		MessageRec message =
			messageHelper.createInstance ()

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
				transaction.now ())

			.setDate (
				transaction.now ().toDateTime ().toLocalDate ())

			.setDeliveryType (
				deliveryType.orNull ())

			.setRef (
				ref)

			.setSubjectText (
				subjectText)

			.setMessageType (
				messageTypeHelper.findByCodeRequired (
					GlobalId.root,
					medias != null
						? "mms"
						: "sms"))

			.setUser (
				user)

			.setNumAttempts (
				0l);

		if (medias != null) {

			for (MediaRec media
					: medias) {

				message.getMedias ().add (
					media);

			}

		}

		if (tags != null) {

			for (
				String tag
					: tags
			) {

				message.getTags ().add (
					tag);

			}

		}

		messageHelper.insert (
			taskLogger,
			message);

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
					sendTime)

				.setRemainingTries (
					route.getMaxTries ()));

		}

		return message;

	}

}
