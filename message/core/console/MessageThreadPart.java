package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.bytesToString;
import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageThreadPart")
public
class MessageThreadPart
	extends AbstractPagePart {

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MessageConsoleStuff messageConsoleStuff;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	TimeFormatter timeFormatter;

	Set<MessageRec> messages;

	@Override
	public
	void prepare () {

		MessageRec message =
			messageHelper.find (
				requestContext.stuffInt ("messageId"));

		messages =
			new TreeSet<MessageRec> (
				messageHelper.findByThreadId (
					message.getThreadId ()));

	}

	@Override
	public
	void goBodyStuff () {

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
			"<th>Media</th>\n",
			"</tr>\n");

		for (MessageRec message
				: messages) {

			// separator

			printFormat (
				"<tr class=\"sep\">\n");

			// various fields

			String rowClass =
				MessageConsoleStuff.classForMessage (
					message);

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td>%h</td>\n",
				message.getId (),

				"<td>%h</td>\n",
				message.getNumFrom (),

				"<td>%h</td>\n",
				message.getNumTo (),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getCreatedTime ())),

				"<td>%h</td>\n",
				message.getRoute ().getCode (),

				"%s\n",
				MessageConsoleStuff.tdForMessageStatus (
					message.getStatus ()));

			List<MediaRec> medias =
				message.getMedias ();

			printFormat (
				"<td rowspan=\"2\">\n");

			for (
				int index = 0;
				index < medias.size ();
				index++
			) {

				MediaRec media =
					medias.get (index);

				if (
					equal (
						media.getMediaType ().getMimeType (),
						"text/plain")
				) {

					printFormat (
						"%h\n",
						bytesToString (
							media.getContent ().getData (),
							media.getEncoding ()));

				} else {

					printFormat (
						"%s\n",
						mediaConsoleLogic.mediaThumb32OrText (
							media));

				}

			}

			printFormat (
				"</td>\n");

			printFormat (
				"</tr>\n");

			// message

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"%s%h</td>\n",
				Html.magicTd (
					requestContext.resolveContextUrl (
						stringFormat (
							"/message",
							"/%u",
							message.getId (),
							"/message_summary")),
					null,
					6),
				messageConsoleStuff.messageSummary (
					message));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
