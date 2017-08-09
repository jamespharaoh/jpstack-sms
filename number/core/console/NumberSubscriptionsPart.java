package wbs.sms.number.core.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.Collection;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.model.NumberRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("numberSubscriptionsPart")
public
class NumberSubscriptionsPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	NumberLinkManager numberLinkManager;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	boolean activeOnly;

	// state

	Collection <NumberPlugin.Link> links;

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

			NumberRec number =
				numberHelper.findFromContextRequired (
					transaction);

			links =
				numberLinkManager.findLinks (
					transaction,
					number,
					activeOnly);

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

			// open table

			formatWriter.writeLineFormat (
				"<table class=\"list\">");

			formatWriter.increaseIndent ();

			// write table header

			formatWriter.writeLineFormat (
				"<tr>");

			formatWriter.increaseIndent ();

			formatWriter.writeLineFormat (
				"<th>Subject</th>");

			formatWriter.writeLineFormat (
				"<th>Subscription</th>");

			formatWriter.writeLineFormat (
				"<th>Started</th>");

			if (! activeOnly) {

				formatWriter.writeLineFormat (
					"<th>Ended</th>");

				formatWriter.writeLineFormat (
					"<th>Active</th>");

			}

			formatWriter.writeLineFormat (
				"<th>Type</th>");

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"</tr>");

			// write empty table contents

			if (links.isEmpty ()) {

				formatWriter.writeLineFormat (
					"<tr>");

				formatWriter.increaseIndent ();

				formatWriter.writeLineFormat (
					"<td colspan=\"%h\">No data to display</td>",
					integerToDecimalString (
						activeOnly ? 4 : 6));

				formatWriter.decreaseIndent ();

				formatWriter.writeLineFormat (
					"</tr>\n");

			}

			// write table contents

			for (
				NumberPlugin.Link link
					: links
			) {

				if (
					! link.canView (
						transaction)
				) {
					continue;
				}

				// open table row

				formatWriter.writeLineFormat (
					"<tr>");

				formatWriter.increaseIndent ();

				// write parent object cell

				objectManager.writeTdForObjectLink (
					transaction,
					formatWriter,
					privChecker,
					link.getParentObject ());

				// write subjectiption object cell

				if (
					isNotNull (
						link.getSubscriptionObject ())
				) {

					objectManager.writeTdForObjectLink (
						transaction,
						formatWriter,
						privChecker,
						link.getSubscriptionObject (),
						link.getParentObject ());

				} else {

					formatWriter.writeLineFormat (
						"<td>—</td>");

				}

				formatWriter.writeLineFormat (
					"<td>%h</td>",
					ifNotNullThenElse (
						link.getStartTime (),
						() -> userConsoleLogic.timestampWithTimezoneString (
							transaction,
							link.getStartTime ()),
						() -> "—"));

				if (! activeOnly) {

					formatWriter.writeLineFormat (
						"<td>%h</td>",
						ifNotNullThenElse (
							link.getEndTime (),
							() -> userConsoleLogic.timestampWithTimezoneString (
								transaction,
								link.getEndTime ()),
							() -> "—"));

					formatWriter.writeLineFormat (
						"<td>%h</td>",
						booleanToYesNo (
							link.getActive ()));

				}

				formatWriter.writeLineFormat (
					"<td>%h</td>",
					link.getType ());

				// close table row

				formatWriter.decreaseIndent ();

				formatWriter.writeLineFormat (
					"</tr>");

			}

			// close table

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"</table>");

		}

	}

}
