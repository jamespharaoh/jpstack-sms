package wbs.sms.number.core.console;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberSubscriptionsPart")
public
class NumberSubscriptionsPart
	extends AbstractPagePart {

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	NumberLinkManager numberLinkManager;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	@Getter @Setter
	boolean activeOnly;

	Collection<NumberPlugin.Link> links;

	@Override
	public
	void prepare () {

		NumberRec number =
			numberHelper.find (
				requestContext.stuffInt ("numberId"));

		links =
			numberLinkManager.findLinks (
				number,
				activeOnly);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Subject</th>\n",
			"<th>Subscription</th>\n",
			"<th>Started</th>\n");

		if (! activeOnly) {

			printFormat (
				"<th>Ended</th>\n",
				"<th>Active</th>\n");

		}

		printFormat (
			"<th>Type</th>\n",
			"</tr>\n");

		if (links.isEmpty ()) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"%h\">No data to display</td>\n",
				activeOnly ? 4 : 6,
				"</tr>\n");

		}

		for (NumberPlugin.Link link : links) {

			if (! link.canView ())
				continue;

			printFormat (
				"<tr>\n");

			printFormat (
				"%s\n",
				objectManager.tdForObjectLink (
					link.getParentObject ()));

			printFormat (
				"%s\n",
				link.getSubscriptionObject () != null
					? objectManager.tdForObjectLink (
						link.getSubscriptionObject (),
						link.getParentObject ())
					: "-");

			printFormat (
				"<td>%h</td>\n",
				link.getStartTime () != null
					? timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						link.getStartTime ())
					: "-");

			if (! activeOnly) {

				printFormat (
					"<td>%h</td>\n",
					link.getEndTime () != null
						? timeFormatter.instantToTimestampString (
							timeFormatter.defaultTimezone (),
							link.getEndTime ())
						: "-");

				printFormat (
					"<td>%h</td>\n",
					link.getActive ()
						? "yes"
						: "no");

			}

			printFormat (
				"<td>%h</td>\n",
				link.getType ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
