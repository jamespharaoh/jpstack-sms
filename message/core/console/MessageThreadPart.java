package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.StringUtils.bytesToString;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageThreadPart")
public
class MessageThreadPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	Set<MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		MessageRec message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		messages =
			new TreeSet<> (
				messageHelper.findByThreadId (
					message.getThreadId ()));

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
				messageConsoleLogic.classForMessage (
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
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()),

				"<td>%h</td>\n",
				message.getRoute ().getCode (),

				"%s\n",
				messageConsoleLogic.tdForMessageStatus (
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
					stringEqualSafe (
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
				"%s%s</td>\n",
				Html.magicTd (
					requestContext.resolveContextUrl (
						stringFormat (
							"/message",
							"/%u",
							message.getId (),
							"/message_summary")),
					null,
					6),
				messageConsoleLogic.messageContentHtml (
					message));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
