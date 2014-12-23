package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.implode;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.FailedMessageObjectHelper;
import wbs.sms.message.outbox.model.FailedMessageRec;

@PrototypeComponent ("messageSummaryPart")
public
class MessageSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MessageConsolePluginManager messageConsolePluginManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FailedMessageObjectHelper failedMessageHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	MessageRec message;
	FailedMessageRec failedMessage;
	MessageConsolePlugin plug;
	PagePart summaryPart;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.find (
				requestContext.stuffInt ("messageId"));

		failedMessage =
			failedMessageHelper.find (
				message.getId ());

		plug =
			messageConsolePluginManager.getPlugin (
				message.getMessageType ().getCode ());

		if (plug != null) {

			summaryPart =
				plug.makeMessageSummaryPart (message);

			summaryPart.setup (
				Collections.<String,Object>emptyMap ());

			summaryPart.prepare ();

		}

	}

	@Override
	public
	void goHeadStuff () {

		if (summaryPart != null)
			summaryPart.goHeadStuff ();

	}

	@Override
	public
	void goBodyStuff () {

		if (message == null) {

			printFormat (
				"<p>Not found</p>\n");

			return;

		}

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr> <th>ID</th> <td>%h</td> </tr>\n",
			message.getId ());

		printFormat (
			"<tr>\n",
			"<th>Thread ID</th>\n",
			"<td>%h</td>\n",
			message.getThreadId (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Other ID</th>\n",
			"<td>%h</td>\n",
			emptyStringIfNull (
				message.getOtherId ()),
			"</tr>\n");

		if (summaryPart != null) {

			summaryPart.goBodyStuff ();

		} else {

			printFormat (
				"<tr>\n",
				"<th>Message</th>\n",

				"<td>%h</td>\n",
				message.getText (),

				"</tr>\n");

		}

		if (message.getDirection () == MessageDirection.in) {

			printFormat (
				"<tr>\n",
				"<th>Number from</th>\n",

				"%s\n",
				objectManager.tdForObject (
					message.getNumber (),
					null,
					true,
					true),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Number to</th>\n",

				"<td>%h</td>\n",
				message.getNumTo (),

				"</tr>\n");

		} else {

			printFormat (
				"<tr> <th>Number from</th> <td>%h</td> </tr>\n",
				message.getNumFrom());

			printFormat (
				"<tr> <th>Number to</th> %s </tr>\n",
				objectManager.tdForObject (
					message.getNumber (),
					null,
					true,
					true));
		}

		printFormat (
			"<tr> <th>Status</th> <td>%h</td> </tr>\n",
			message.getStatus ().getDescription ());

		printFormat (
			"<tr> <th>Direction</th> <td>%h</td> </tr>\n",
			message.getDirection ());

		printFormat (
			"<tr> <th>Route</th> %s </tr>\n",
			objectManager.tdForObject (
				message.getRoute (),
				null,
				true,
				true));

		printFormat (
			"<tr> <th>Network</th> <td>%h</td> </tr>\n",
			message.getNetwork ().getDescription ());

		printFormat (
			"<tr> <th>Service</th> %s </tr>\n",
			objectManager.tdForObject (
				message.getService (),
				null,
				true,
				true));

		printFormat (
			"<tr> <th>Affiliate</th> %s </tr>\n",
			objectManager.tdForObject (
				message.getAffiliate (),
				null,
				true,
				true));

		if (message.getDirection () == MessageDirection.in) {

			printFormat (
				"<tr>\n",
				"<th>Time sent</th>\n",

				"<td>%h</td>\n",
				message.getNetworkTime () != null
					? timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							message.getNetworkTime ()))
					: "-",

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time received</th>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getCreatedTime ())),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time processed</th>\n",

				"<td>%h</td>\n",
				message.getProcessedTime () != null
					? timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							message.getProcessedTime ()))
					: "-",

				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<th>Command</th>\n",

				"%s\n",
				objectManager.tdForObject (
					message.getCommand (),
					null,
					true,
					true),

				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Time created</th>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getCreatedTime ())),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time sent</th>\n",

				"<td>%h</td>\n",
				message.getProcessedTime () != null
					? timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							message.getProcessedTime ()))
					: "-",

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time received</th>\n",

				"<td>%h</td>\n",
				message.getNetworkTime () != null
					? timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							message.getNetworkTime ()))
					: "-",

				"</tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>Charge</th>\n",

			"<td>%h</td>\n",
			message.getCharge (),

			"</tr>\n");

		List<MediaRec> medias =
			message.getMedias ();

		if (medias.size () > 0) {

			printFormat (
				"<tr>\n",
				"<th>Media</th>\n",
				"<td>\n");

			for (
				int index = 0;
				index < medias.size ();
				index ++
			) {

				MediaRec media =
					medias.get (index);

				printFormat (
					"<a href=\"%h\">%s</a>\n",

					requestContext.resolveContextUrl (
						stringFormat (
							"/message_media",
							"/%d",
							message.getId (),
							"/%d",
							index,
							"/message_media_summary")),

					mediaConsoleLogic.mediaThumb100 (
						media));

			}

			printFormat (
				"</td> </tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>Tags</th>\n",
			"<td>%h</td>\n",
			implode (", ", message.getTags ()),
			"</tr>\n");

		if (failedMessage != null) {

			printFormat (
				"<tr> <th>Failure reason</th> <td>%h</td> </tr>\n",
				failedMessage.getError ());

		}

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObject (
				message.getUser (),
				null,
				true,
				true),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
