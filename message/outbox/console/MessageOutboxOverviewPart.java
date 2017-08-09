package wbs.sms.message.outbox.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.forms.core.FormFieldSet;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.outbox.model.RouteOutboxSummaryObjectHelper;
import wbs.sms.message.outbox.model.RouteOutboxSummaryRec;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.duration.DurationFormatter;

@PrototypeComponent ("messageOutboxOverviewPart")
public
class MessageOutboxOverviewPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DurationFormatter durationFormatter;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency
	ConsoleModule messageOutboxConsoleModule;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteOutboxSummaryObjectHelper routeOutboxSummaryHelper;

	// state

	FormFieldSet <RouteOutboxSummaryRec> fields;

	List <RouteOutboxSummaryRec> routeOutboxSummaries;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			fields =
				messageOutboxConsoleModule.formFieldSetRequired (
					"routeOutboxSummary",
					RouteOutboxSummaryRec.class);

			routeOutboxSummaries =
				routeOutboxSummaryHelper.findAll (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
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
					formatWriter,
					htmlClassAttribute (
						"magic-table-row"),
					htmlDataAttribute (
						"target-href",
						requestContext.resolveLocalUrlFormat (
							"/outbox.route",
							"?routeId=%u",
							integerToDecimalString (
								route.getId ()))));

				htmlTableCellWrite (
					formatWriter,
					route.getCode ());

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						routeOutboxSummary.getNumMessages ()));

				htmlTableCellWrite (
					formatWriter,
					durationFormatter.durationStringApproximate (
						routeOutboxSummary.getOldestTime (),
						transaction.now ()));

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
