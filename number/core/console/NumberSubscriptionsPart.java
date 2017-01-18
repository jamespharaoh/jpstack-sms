package wbs.sms.number.core.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.Collection;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.model.NumberRec;

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
			@NonNull TaskLogger parentTaskLogger) {

		NumberRec number =
			numberHelper.findFromContextRequired ();

		links =
			numberLinkManager.findLinks (
				number,
				activeOnly);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

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

			if (! link.canView ()) {
				continue;
			}

			// open table row

			formatWriter.writeLineFormat (
				"<tr>");

			formatWriter.increaseIndent ();

			// write parent object cell

			objectManager.writeTdForObjectLink (
				taskLogger,
				formatWriter,
				link.getParentObject ());

			// write subjectiption object cell

			if (
				isNotNull (
					link.getSubscriptionObject ())
			) {

				objectManager.writeTdForObjectLink (
					taskLogger,
					formatWriter,
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
						link.getStartTime ()),
					() -> "—"));

			if (! activeOnly) {

				formatWriter.writeLineFormat (
					"<td>%h</td>",
					ifNotNullThenElse (
						link.getEndTime (),
						() -> userConsoleLogic.timestampWithTimezoneString (
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
