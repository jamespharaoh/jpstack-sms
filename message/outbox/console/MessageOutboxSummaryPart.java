package wbs.sms.message.outbox.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.console.html.JqueryScriptRef;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.outbox.model.RouteOutboxSummaryObjectHelper;
import wbs.sms.message.outbox.model.RouteOutboxSummaryRec;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("messageOutboxSummaryPart")
public
class MessageOutboxSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	Database database;

	@Inject
	RouteOutboxSummaryObjectHelper routeOutboxSummaryHelper;

	// state

	List<RouteOutboxSummaryRec> routeOutboxSummaries;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		routeOutboxSummaries =
			routeOutboxSummaryHelper.findAll ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		Transaction transaction =
			database.currentTransaction ();

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Route</th>\n",
			"<th>Messages</th>\n",
			"<th>Oldest</th>\n",
			"</tr>\n");

		for (
			RouteOutboxSummaryRec routeOutboxSummary
				: routeOutboxSummaries
		) {

			RouteRec route =
				routeOutboxSummary.getRoute ();

			printFormat (
				"<tr",
				" class=\"magic-table-row\"",

				" data-target-href=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/outbox.route",
						"?routeId=%u",
						route.getId ())),

				">\n");

			printFormat (
				"<td>%h</td>\n",
				route.getCode ());

			printFormat (
				"<td>%h</td>\n",
				routeOutboxSummary.getNumMessages ());

			printFormat (
				"<td>%h</td>\n",
				requestContext.prettyDateDiff (
					dateToInstant (
						routeOutboxSummary.getOldestTime ()),
					transaction.now ()));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
