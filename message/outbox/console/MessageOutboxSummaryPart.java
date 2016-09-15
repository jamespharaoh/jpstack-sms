package wbs.sms.message.outbox.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.outbox.model.RouteOutboxSummaryObjectHelper;
import wbs.sms.message.outbox.model.RouteOutboxSummaryRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("messageOutboxSummaryPart")
public
class MessageOutboxSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	RouteOutboxSummaryObjectHelper routeOutboxSummaryHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

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
				timeFormatter.prettyDuration (
					routeOutboxSummary.getOldestTime (),
					transaction.now ()));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
