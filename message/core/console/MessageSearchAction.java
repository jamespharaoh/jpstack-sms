package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.parseTimeAfter;
import static wbs.framework.utils.etc.Misc.parseTimeBefore;
import static wbs.framework.utils.etc.Misc.pluralise;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.Misc.toInteger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.TimeFormatException;
import wbs.framework.web.Responder;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("messageSearchAction")
public
class MessageSearchAction
	extends ConsoleAction {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	PrivChecker privChecker;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	protected
	Responder backupResponder () {

		return responder ("messageSearchResponder");

	}

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// get parameters

		String idParam =
			nullIfEmptyString (
				requestContext.parameter ("id"));

		String numberParam =
			nullIfEmptyString (
				requestContext.parameter ("number"));

		Integer routeIdParam =
			toInteger (requestContext.parameter ("routeId"));

		Integer statusParam =
			toInteger (requestContext.parameter ("status"));

		Integer serviceIdParam =
			toInteger (requestContext.parameter ("serviceId"));

		Integer affiliateIdParam =
			toInteger (requestContext.parameter ("affiliateId"));

		String dateParam =
			nullIfEmptyString (
				requestContext.parameter ("date"));

		String timeFromParam =
			nullIfEmptyString (
				requestContext.parameter ("timeFrom"));

		String timeToParam =
			nullIfEmptyString (
				requestContext.parameter ("timeTo"));

		String messageParam =
			nullIfEmptyString (
				requestContext.parameter ("message"));

		String outputTypeParam =
			requestContext.parameter ("outputType");

		Integer networkIdParam =
			toInteger (requestContext.parameter ("networkId"));

		MessageDirection directionParam =
			toEnum (
				MessageDirection.class,
				requestContext.parameter ("direction"));

		Integer userIdParam =
			toInteger (requestContext.parameter ("userId"));

		// save in session

		requestContext.session ("messageSearchId", idParam);
		requestContext.session ("messageSearchNumber", numberParam);
		requestContext.session ("messageSearchRouteId", routeIdParam);
		requestContext.session ("messageSearchStatus", statusParam);
		requestContext.session ("messageSearchServiceId", serviceIdParam);
		requestContext.session ("messageSearchAffiliateId", affiliateIdParam);
		requestContext.session ("messageSearchDate", dateParam);
		requestContext.session ("messageSearchTimeAfter", timeFromParam);
		requestContext.session ("messageSearchTimeBefore", timeToParam);
		requestContext.session ("messageSearchMessage", messageParam);
		requestContext.session ("messageSearchDirection", directionParam);
		requestContext.session ("messageSearchNetworkId", networkIdParam);
		requestContext.session ("messageSearchUserId", userIdParam);

		// check the message status

		MessageStatus status =
			statusParam != null
				? MessageStatus.fromInt (statusParam)
				: null;

		// check the id

		Integer id =
			toInteger (idParam);

		if (idParam != null && id == null) {

			requestContext.addError (
				"Please enter a valid message id");

			return null;

		}

		// see if we have a date/time problem

		if (dateParam != null
				&& (timeFromParam != null
					|| timeToParam != null)) {

			requestContext.addError (
				"Please specify EITHER a date OR a time range (not both!)");

			return null;

		}

		// work out our dates

		Date timeFrom = null;
		Date timeTo = null;

		try {

			if (dateParam != null) {

				timeFrom =
					instantToDate (
						parseTimeAfter (
							dateParam));

				timeTo =
					instantToDate (
						parseTimeBefore (
							dateParam));

			} else {

				if (timeFromParam != null) {

					timeFrom =
						instantToDate (
							parseTimeAfter (
								timeFromParam));

				}

				if (timeToParam != null) {

					timeTo =
						instantToDate (
							parseTimeBefore (
								timeToParam));

				}

			}

		} catch (TimeFormatException exception) {

			requestContext.addError ("Invalid date/time");

			return null;

		}

		MessageSearch search =
			new MessageSearch ()
				.orderBy (MessageSearchOrder.createdTimeDesc);

		if (id != null)
			search.id (id);

		if (numberParam != null)
			search.number (numberParam);

		if (routeIdParam != null)
			search.routeId (routeIdParam);

		if (serviceIdParam != null)
			search.serviceId (serviceIdParam);

		if (status != null)
			search.status (status);

		if (timeFrom != null)
			search.createdTimeAfter (
				dateToInstant (timeFrom));

		if (timeTo != null)
			search.createdTimeBefore (
				dateToInstant (timeTo));

		if (messageParam != null)
			search.textILike ("%" + messageParam + "%");

		if (directionParam != null)
			search.direction (directionParam);

		if (networkIdParam != null)
			search.networkId (networkIdParam);

		if (userIdParam != null)
			search.userId (userIdParam);

		setFilter (
			requestContext,
			search);

		List<MessageRec> messages =
			messageHelper.search (
				search);

		if (messages.size () == 0) {

			requestContext.addError (
				"Search produced no results");

			return null;

		}

		requestContext.addNotice (
			stringFormat (
				"Found %s",
				pluralise (messages.size (), "message")));

		List<Integer> messageIds =
			new ArrayList<Integer> ();

		for (MessageRec message
				: messages) {

			messageIds.add (
				message.getId ());

		}

		requestContext.request (
			"messageSearchResult",
			messageIds);

		if (equal (outputTypeParam, "HTML")) {

			return responder ("messageSearchResultsResponder");

		}

		// TODO more checking?

		return responder ("messageSearchResultsCsvResponder");

	}

	private
	void setFilter (
			ConsoleRequestContext requestContext,
			MessageSearch search) {

		search.filter (true);

		// services

		search.filterServiceIds (
			new ArrayList<Integer> ());

		search.filterServiceIds ().add (-1);

		for (ServiceRec service
				: serviceHelper.findAll ()) {

			if (privChecker.can (
					objectManager.getParent (service),
					"messages")) {

				search.filterServiceIds ().add (
					service.getId ());

			}

		}

		// affiliates

		search.filterAffiliateIds (
			new ArrayList<Integer> ());

		search.filterAffiliateIds ().add (-1);

		for (AffiliateRec affiliate
				: affiliateHelper.findAll ()) {

			if (privChecker.can (
					objectManager.getParent (affiliate),
					"messages")) {

				search.filterAffiliateIds ().add (
					affiliate.getId ());

			}

		}

		// routes

		search.filterRouteIds (
			new ArrayList<Integer> ());

		search.filterRouteIds ().add (-1);

		for (RouteRec route
				: routeHelper.findAll ()) {

			if (! privChecker.can (
					route,
					"messages"))
				continue;

			search.filterRouteIds ().add (
				route.getId ());

		}

	}

}
