package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.nullIf;
import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.timestampFormatSeconds;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
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
	MessageConsolePluginManager messageConsolePluginManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	// implementatino

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

		for (Object messageIdObject
				: messageSearchResult) {

			MessageRec message =
				messageHelper.find (
					(Integer) messageIdObject);

			List<MediaRec> medias =
				message.getMedias ();

			MessageConsolePlugin messageConsolePlugin =
				messageConsolePluginManager.getPlugin (
					message.getMessageType ().getCode ());

			PagePart summaryPart = null;

			if (messageConsolePlugin != null) {

				summaryPart =
					messageConsolePlugin.makeMessageSummaryPart (
						message);

				summaryPart.setWithMarkup (false);

				summaryPart.setup (
					Collections.<String,Object>emptyMap ());

				summaryPart.prepare ();

			}

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

			if (summaryPart != null) {

				summaryPart.renderHtmlBodyContent ();

			} else if (! medias.isEmpty ()) {

				if (nullIf (message.getSubjectText (), "") != null) {

					printFormat (
						"%h:\n",
						message.getSubjectText ());

				}

				int index = 0;

				for (MediaRec media : medias) {

					if (
						mediaLogic.isText (
							media.getMediaType ().getMimeType ())
					) {

						printFormat (
							"%s\n",
							mediaConsoleLogic.mediaThumb32OrText (
								media));

					} else {

						printFormat (
							"<a href=\"%h\">%s</a>\n",

							requestContext.resolveContextUrl (
								stringFormat (
									"/message",
									"/%u",
									message.getId (),
									"/message_mediaSummary",
									"?index=%u",
									index ++)),

							mediaConsoleLogic.mediaThumb32OrText (
								media));

					}

				}

			} else {

				printFormat (
					"%h",
					spacify (
						message.getText ().getText ()));

			}

			printFormat (
				"</td>\n");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
