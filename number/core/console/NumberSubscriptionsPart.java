package wbs.sms.number.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
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
	void goBodyStuff () {

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
				objectManager.tdForObject (
					link.getParentObject (),
					null,
					false,
					true));

			printFormat (
				"%s\n",
				link.getSubscriptionObject () != null
					? objectManager.tdForObject (
						link.getSubscriptionObject (),
						link.getParentObject (),
						false,
						true)
					: "-",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (link.getStartTime ())));

			if (! activeOnly) {

				printFormat (
					"<td>%h</td>\n",
					timeFormatter.instantToTimestampString (
						dateToInstant (link.getEndTime ())),

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
