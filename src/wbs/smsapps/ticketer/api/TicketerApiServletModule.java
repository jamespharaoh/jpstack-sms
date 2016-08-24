package wbs.smsapps.ticketer.api;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import wbs.api.mvc.ApiFile;
import wbs.api.mvc.StringMapResponderFactory;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.Action;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.rpc.php.PhpStringMapResponderFactory;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.ticketer.model.TicketerObjectHelper;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketObjectHelper;
import wbs.smsapps.ticketer.model.TicketerTicketRec;

@SingletonComponent ("ticketerApiServletModule")
public
class TicketerApiServletModule
	implements ServletModule {

	// dependencies

	@Inject
	RequestContext requestContext;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	Database database;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TicketerObjectHelper ticketerHelper;

	@Inject
	TicketerTicketObjectHelper ticketerTicketHelper;

	@Inject
	Provider<ApiFile> apiFile;

	// ================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {
		return null;
	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()

			.put ("/ticketer/query/php",
				apiFile.get ()
					.getAction (queryPhpAction)
					.postAction (queryPhpAction))

			.build ();

	}

	// ================================= actions

	Action queryPhpAction =
		new QueryAction (
			new PhpStringMapResponderFactory ());

	// ================================= query action

	public static final
	int stRequestInvalid = 0x01;

	public static final
	int stTicketerInvalid = 0x02;

	public static final
	int stTicketInvalid = 0x10;

	public static final
	int stTicketExpired = 0x11;

	public static final
	int stTicketValidPermanently = 0x20;

	public static final
	int stTicketValidTemporary = 0x21;

	public static final
	int stInternalError = 0xff;

	class QueryAction
		implements Action {

		StringMapResponderFactory responderFactory;

		QueryAction (
				StringMapResponderFactory newResponderFactory) {

			responderFactory =
				newResponderFactory;

		}

		Map<String,Object> makeError (
				int status,
				String statusCode,
				String message) {

			return ImmutableMap.<String,Object>builder ()
				.put ("status", status)
				.put ("status-code", statusCode)
				.put ("valid", false)
				.put ("message", message)
				.build ();

		}

		public
		Map<String,Object> myGo (
				RequestContext requestContext) {

			String sliceParam =
				requestContext.parameterOrNull ("slice");

			String codeParam =
				requestContext.parameterOrNull ("code");

			String numberParam =
				requestContext.parameterOrNull ("number");

			String ticketParam =
				requestContext.parameterOrNull ("ticket");

			if (
				stringIsEmpty (
					emptyStringIfNull (
						sliceParam))
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param slice must be supplied");

			}

			if (
				stringIsEmpty (
					emptyStringIfNull (
						codeParam))
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param code must be supplied");

			}

			if (
				stringIsEmpty (
					emptyStringIfNull (
						numberParam))
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param number must be supplied");

			}

			if (
				stringIsEmpty (
					emptyStringIfNull (
						ticketParam))
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param ticket must be supplied");

			}

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"TicketerApiServletModule.QueryAction.myGo ()",
					this);

			Optional<SliceRec> sliceOptional =
				sliceHelper.findByCode (
					GlobalId.root,
					sliceParam);

			if (
				isNotPresent (
					sliceOptional)
			) {

				return makeError (
					stTicketerInvalid,
					"ticketer-invalid",
					"Params slice/code are not valid\n");

			}

			SliceRec slice =
				sliceOptional.get ();

			Optional<TicketerRec> ticketerOptional =
				ticketerHelper.findByCode (
					slice,
					codeParam);

			if (
				isNotPresent (
					ticketerOptional)
			) {

				return makeError (
					stTicketerInvalid,
					"ticketer-invalid",
					"Params slice/code are not valid\n");

			}

			TicketerRec ticketer =
				ticketerOptional.get ();

			Optional<NumberRec> numberOptional =
				numberHelper.findByCode (
					GlobalId.root,
					numberParam);

			if (
				isNotPresent (
					numberOptional)
			) {

				return makeError (
					stTicketInvalid,
					"ticket-invalid",
					"Ticket is not valid");

			}

			NumberRec number =
				numberOptional.get ();

			TicketerTicketRec ticket =
				ticketerTicketHelper.findByTicket (
					ticketer,
					number,
					ticketParam);

			if (ticket == null) {

				return makeError (
					stTicketInvalid,
					"ticket-invalid",
					"Ticket is not valid");

			}

			if (ticket.getRetrievedTime () == null) {

				// new ticket

				ticket

					.setRetrievedTime (
						transaction.now ())

					.setExpiresTime (
						transaction.now ().plus (
							Duration.standardSeconds (
								ticketer.getDuration ())));

				transaction.commit ();

				return ImmutableMap.<String,Object>builder ()
					.put ("status", stTicketValidTemporary)
					.put ("status-code", "ticket-valid-temporary")
					.put ("valid", true)
					.put ("message", "Ticket is valid for "
							+ ticketer.getDuration() + " seconds")
					.put ("time-left", ticketer.getDuration ())
					.build ();

			} else {

				// old ticket

				boolean expired =
					earlierThan (
						ticket.getExpiresTime (),
						transaction.now ());

				transaction.commit ();

				if (expired) {

					return ImmutableMap.<String,Object>builder ()
						.put ("status", stTicketExpired)
						.put ("status-code", "ticket-expired")
						.put ("valid", false)
						.put ("message", "Ticket has expired")
						.build ();

				}

				return ImmutableMap.<String,Object>builder ()
					.put ("status", stTicketValidPermanently)
					.put ("status-code", "ticket-valid-permanently")
					.put ("valid", true)
					.put ("message", "Ticket is valid permanently")
					.build ();

			}

		}

		@Override
		public
		Responder handle () {

			try {

				Map<String,Object> map =
					myGo (
						requestContext);

				return responderFactory.makeResponder (
					map);

			} catch (RuntimeException exception) {

				exceptionLogger.logThrowable (
					"webapi",
					requestContext.requestUri (),
					exception,
					Optional.absent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

				requestContext.status (500);

				Map<String,Object> map =
					ImmutableMap.<String,Object>builder ()

					.put (
						"status",
						stInternalError)

					.put (
						"status-code",
						"internal-error")

					.put (
						"valid",
						false)

					.put (
						"message",
						"An internal error has occurred")

					.build ();

				return responderFactory.makeResponder (
					map);

			}

		}

	}

}
