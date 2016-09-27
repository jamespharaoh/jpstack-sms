package wbs.sms.message.outbox.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

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

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Route",
			"Messages",
			"Oldest");

		for (
			RouteOutboxSummaryRec routeOutboxSummary
				: routeOutboxSummaries
		) {

			RouteRec route =
				routeOutboxSummary.getRoute ();

			htmlTableRowOpen (
				htmlClassAttribute (
					"magic-table-row"),
				htmlDataAttribute (
					"target-href",
					requestContext.resolveLocalUrl (
						stringFormat (
							"/outbox.route",
							"?routeId=%u",
							route.getId ()))));

			htmlTableCellWrite (
				route.getCode ());

			htmlTableCellWrite (
				integerToDecimalString (
					routeOutboxSummary.getNumMessages ()));

			htmlTableCellWrite (
				timeFormatter.prettyDuration (
					routeOutboxSummary.getOldestTime (),
					transaction.now ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
