package wbs.sms.message.outbox.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.outbox.model.RouteOutboxSummaryObjectHelper;
import wbs.sms.message.outbox.model.RouteOutboxSummaryRec;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("messageOutboxSummaryPart")
public
class MessageOutboxSummaryPart
	extends AbstractPagePart {

	@Inject
	RouteOutboxSummaryObjectHelper routeOutboxSummaryHelper;

	List<RouteOutboxSummaryRec> routeOutboxSummaries;

	@Override
	public
	void prepare () {

		routeOutboxSummaries =
			routeOutboxSummaryHelper.findAll ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		Date now =
			new Date ();

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Route</th>\n",
			"<th>Messages</th>\n",
			"<th>Oldest</th>\n",
			"</tr>\n");

		for (RouteOutboxSummaryRec routeOutboxSummary
				: routeOutboxSummaries) {

			RouteRec route =
				routeOutboxSummary.getRoute ();

			printFormat (
				"%s\n",
				Html.magicTr (
					requestContext.resolveLocalUrl (
						stringFormat (
							"/outbox.route",
							"?routeId=%u",
							route.getId ())),
					false));

			printFormat (
				"<td>%h</td>\n",
				route.getCode ());

			printFormat (
				"<td>%h</td>\n",
				routeOutboxSummary.getNumMessages ());

			printFormat (
				"<td>%h</td>\n",
				requestContext.prettyDateDiff (
					routeOutboxSummary.getOldestTime (),
					now));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
