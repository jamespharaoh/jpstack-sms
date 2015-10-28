package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.timestampFormatSeconds;

import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageSearchResultsPart")
public
class MessageSearchResultsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageConsolePluginManager messageConsolePluginManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	// implementation

	@Override
	public
	void renderHtmlBodyContent () {

		List<?> messageSearchResult =
			(List<?>)
			requestContext.request ("messageSearchResult");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>ID</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Time</th>\n",
			"<th>Route</th>\n",
			"<th>Status</th>\n",
			"<th>Service</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (
			Object messageIdObject
				: messageSearchResult
		) {

			MessageRec message =
				messageHelper.find (
					(Integer) messageIdObject);

			String summaryHtml =
				messageConsoleLogic.messageContentHtml (
					message);

			printFormat (
				"<tr class=\"sep\">\n");

			String rowClass =
				MessageConsoleStuff.classForMessage (
					message);

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"<td>%h</td>\n",
				message.getId ());

			printFormat (
				"<td>%h</td>\n",
				message.getDirection () == MessageDirection.in
					? message.getNumber ().getNumber ()
					: message.getNumFrom ());

			printFormat (
				"<td>%h</td>\n",
				message.getDirection () == MessageDirection.out
					? message.getNumber ().getNumber ()
					: message.getNumTo ());

			printFormat (
				"<td>%h</td>\n",
				timestampFormatSeconds.format (
					message.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				message.getRoute ().getCode ());

			printFormat (
				"%s\n",
				MessageConsoleStuff.tdForMessageStatus (
					message.getStatus ()));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getService ()));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getUser ()));

			printFormat (
				"</tr>\n");

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"%s\n",
				Html.magicTd (
					requestContext.resolveContextUrl (
						stringFormat (
							"/message",
							"/%s",
							message.getId (),
							"/message.summary")),
					null,
					8));

			printFormat (
				"%s\n",
				summaryHtml);

			printFormat (
				"</td>\n");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
