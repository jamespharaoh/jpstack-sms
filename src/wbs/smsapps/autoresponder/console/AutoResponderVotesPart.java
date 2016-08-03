package wbs.smsapps.autoresponder.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.StringUtils.simplify;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.IntervalFormatter;
import wbs.framework.utils.TextualInterval;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;

@Log4j
@PrototypeComponent ("autoResponderVotesPart")
public
class AutoResponderVotesPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	AutoResponderObjectHelper autoResponderHelper;

	@Inject
	IntervalFormatter intervalFormatter;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	String timePeriodString;
	Map<String,Integer> votes;

	// implementation

	@Override
	public
	void prepare () {

		// check units

		timePeriodString =
			requestContext.parameterOrDefault (
				"timePeriod",
				"12 hours");

		Integer timePeriodSeconds =
			intervalFormatter.parseIntervalStringSecondsRequired (
				timePeriodString);

		if (timePeriodSeconds == null) {

			requestContext.addError (
				"Invalid time period");

			return;

		}

		// lookup objects

		AutoResponderRec autoResponder =
			autoResponderHelper.findRequired (
				requestContext.stuffInt (
					"autoResponderId"));

		ServiceRec autoResponderService =
			serviceHelper.findByCodeRequired (
				autoResponder,
				"default");

		// workout start time

		Instant startTime =
			transaction.now ().minus (
				Duration.standardSeconds (
					timePeriodSeconds));

		log.info (
			stringFormat (
				"Searching from %s",
				startTime));

		// retrieve messages

		MessageSearch messageSearch =
			new MessageSearch ()

			.serviceId (
				autoResponderService.getId ())

			.createdTime (
				TextualInterval.after (
					userConsoleLogic.timezone (),
					startTime))

			.direction (
				MessageDirection.in);

		List<MessageRec> messages =
			messageHelper.search (
				messageSearch);

		// now aggregate them

		votes =
			new TreeMap<String,Integer> ();

		for (
			MessageRec message
				: messages
		) {

			String body =
				simplify (
					message.getText ().getText ());

			Integer oldVal =
				ifNull (votes.get (body), 0);

			votes.put (
				body,
				oldVal + 1);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"\">\n");

		printFormat (
			"<p>Time period<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"timePeriod\"",
			" value=\"%h\"",
			timePeriodString,
			"\">",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">",

			"</p>\n");

		printFormat (
			"</form>\n");

		if (votes == null)
			return;

		printFormat (
			"<h2>Vote summary</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Content</th>\n",
			"<th>Votes</th>\n",
			"</tr>");

		for (Map.Entry<String,Integer> entry
				: votes.entrySet ()) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				entry.getKey (),

				"<td>%h</td>\n",
				entry.getValue (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
