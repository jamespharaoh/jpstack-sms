package wbs.sms.message.report.logic;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.optionalEquals;
import static wbs.framework.utils.etc.Misc.optionalIn;
import static wbs.framework.utils.etc.Misc.optionalNotIn;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.ReadableInstant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDao;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("reportLogic")
public
class ReportLogicImplementation
	implements ReportLogic {

	@Inject
	Database database;

	@Inject
	MessageDao messageDao;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	MessageReportObjectHelper messageReportHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	RouteObjectHelper routeHelper;

	@Override
	public
	void deliveryReport (
			@NonNull MessageRec message,
			@NonNull Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException {

		Transaction transaction =
			database.currentTransaction ();

		// check arguments

		if (
			optionalNotIn (
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
				newMessageStatus.or (
					message.getStatus ()))

			.setMessageReportCode (
				messageReportCode)

		);

		// update received time if appropriate

		if (

			optionalEquals (
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

		if (newMessageStatus != null) {

			switch (message.getStatus ()) {

			case sent:

				if (
					optionalIn (
						newMessageStatus,
						MessageStatus.sent,
						MessageStatus.submitted,
						MessageStatus.undelivered,
						MessageStatus.delivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus.get ());

				}

				break;

			case submitted:

				if (
					optionalIn (
						newMessageStatus,
						MessageStatus.submitted,
						MessageStatus.delivered,
						MessageStatus.undelivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus.get ());

				}

				break;

			case reportTimedOut:

				if (
					optionalIn (
						newMessageStatus,
						MessageStatus.delivered,
						MessageStatus.undelivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus.get ());

				}

				break;

			case undelivered:

				if (
					optionalIn (
						newMessageStatus,
						MessageStatus.delivered)
				) {

					messageLogic.messageStatus (
						message,
						newMessageStatus.get ());

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
				"DLV %s %s %s %s (%s)",
				message.getId (),
				message.getRoute ().getCode (),
				message.getOtherId (),
				message.getStatus (),
				ifNull (
					messageReportCode != null
						? messageReportCode.getDescription ()
						: "",
					"")));

	}

	@Override
	public
	MessageRec deliveryReport (
			@NonNull RouteRec route,
			@NonNull String otherId,
			@NonNull Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
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
			timestamp,
			messageReportCode);

		return message;

	}

	@Override
	public
	void deliveryReport (
			int messageId,
			Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException {

		// lookup the message

		MessageRec message =
			messageHelper.find (
				messageId);

		if (message == null) {

			throw new NoSuchMessageException (
				stringFormat (
					"Message ID: %s"));

		}

		// process the report

		deliveryReport (
			message,
			newMessageStatus,
			timestamp,
			messageReportCode);

	}

}
