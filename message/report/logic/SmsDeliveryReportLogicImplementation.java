package wbs.sms.message.report.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.ReadableInstant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDao;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.model.MessageReportObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("smsDeliveryReportLogic")
public
class SmsDeliveryReportLogicImplementation
	implements SmsDeliveryReportLogic {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	MessageDao messageDao;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	MessageReportObjectHelper messageReportHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void deliveryReport (
			@NonNull MessageRec message,
			@NonNull MessageStatus newMessageStatus,
			@NonNull Optional <String> theirCode,
			@NonNull Optional <String> theirDescription,
			@NonNull Optional <String> extraInformation,
			@NonNull Optional <ReadableInstant> theirTimestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException {

		Transaction transaction =
			database.currentTransaction ();

		// check arguments

		if (
			enumNotInSafe (
				newMessageStatus,
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.undelivered,
				MessageStatus.delivered)
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"Invalid newMessageStatus %s for message %d",
					newMessageStatus,
					message.getId ()));

		}

		// check delivery reports are enabled

		if (! message.getRoute ().getDeliveryReports ()) {

			throw new RuntimeException (
				stringFormat (
					"Not expecting delivery reports on %s",
					objectManager.objectPath (message.getRoute ())));

		}

		// create message report thingy

		messageReportHelper.insert (
			messageReportHelper.createInstance ()

			.setMessage (
				message)

			.setReceivedTime (
				transaction.now ())

			.setNewMessageStatus (
				newMessageStatus)

			.setTheirCode (
				textHelper.findOrCreateMapNull (
					optionalOrNull (
						theirCode)))

			.setTheirDescription (
				textHelper.findOrCreateMapNull (
					optionalOrNull (
						theirDescription)))

			.setTheirTimestamp (
				optionalOrNull (
					theirTimestamp))

		);

		// update received time if appropriate

		if (

			enumEqualSafe (
				newMessageStatus,
				MessageStatus.delivered)

			&& isNull (
				message.getProcessedTime ())

		) {

			message

				.setProcessedTime (
					transaction.now ());

		}

		// depending on the new and old status, update it

		if (
			isNotNull (
				newMessageStatus != null)
		) {

			switch (message.getStatus ()) {

			case sent:

				if (
					enumInSafe (
						newMessageStatus,
						MessageStatus.sent,
						MessageStatus.submitted,
						MessageStatus.undelivered,
						MessageStatus.delivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus);

				}

				break;

			case submitted:

				if (
					enumInSafe (
						newMessageStatus,
						MessageStatus.submitted,
						MessageStatus.delivered,
						MessageStatus.undelivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus);

				}

				break;

			case reportTimedOut:

				if (
					enumInSafe (
						newMessageStatus,
						MessageStatus.delivered,
						MessageStatus.undelivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus);

				}

				break;

			case undelivered:

				if (
					enumInSafe (
						newMessageStatus,
						MessageStatus.delivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus);

				}

				break;

			case delivered:
			case manuallyUndelivered:

				break;

			default:

				throw new InvalidMessageStateException (
					stringFormat (
						"Message %d has status %s",
						message.getId (),
						message.getStatus ()));

			}

		}

		// write to log file

		log.info (
			stringFormat (
				"DLV %s %s %s %s",
				message.getId (),
				message.getRoute ().getCode (),
				ifNull (
					message.getOtherId (),
					"—"),
				message.getStatus ()));

		// update simulated multipart messages

		message.getMultipartCompanionLinks ().stream ()

			.filter (
				link ->
					link.getSimulated ())

			.forEach (
				link ->
					deliveryReport (
						link.getMessage (),
						newMessageStatus,
						theirCode,
						theirDescription,
						extraInformation,
						theirTimestamp));

	}

	@Override
	public
	MessageRec deliveryReport (
			@NonNull RouteRec route,
			@NonNull String otherId,
			@NonNull MessageStatus newMessageStatus,
			@NonNull Optional<String> theirCode,
			@NonNull Optional<String> theirDescription,
			@NonNull Optional<String> extraInformation,
			@NonNull Optional<ReadableInstant> theirTimestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException {

		// lookup the message

		MessageRec message =
			messageHelper.findByOtherId (
				MessageDirection.out,
				route,
				otherId);

		if (message == null) {

			throw new NoSuchMessageException (
				stringFormat (
					"Delivery report for unrecognised message id %s ",
					otherId,
					"on route %s (%d)",
					route.getCode (),
					route.getId ()));

		}

		// process the report

		deliveryReport (
			message,
			newMessageStatus,
			theirCode,
			theirDescription,
			extraInformation,
			theirTimestamp);

		return message;

	}

	@Override
	public
	void deliveryReport (
			@NonNull Long messageId,
			@NonNull MessageStatus newMessageStatus,
			@NonNull Optional <String> theirCode,
			@NonNull Optional <String> theirDescription,
			@NonNull Optional <String> extraInformation,
			@NonNull Optional <ReadableInstant> theirTimestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException {

		// lookup the message

		MessageRec message =
			messageHelper.findOrThrow (
				messageId,
				() -> new NoSuchMessageException (
					stringFormat (
						"Message ID: %s")));

		// process the report

		deliveryReport (
			message,
			newMessageStatus,
			theirCode,
			theirDescription,
			extraInformation,
			theirTimestamp);

	}

}
