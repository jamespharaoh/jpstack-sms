package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.implode;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;
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
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FailedMessageObjectHelper failedMessageHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;
	Optional<FailedMessageRec> failedMessage;
	MessageConsolePlugin plug;
	String summaryHtml;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInt (
					"messageId"));

		failedMessage =
			failedMessageHelper.find (
				message.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

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

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td>%s</td>\n",
			messageConsoleLogic.messageContentHtml (
				message),

			"</tr>\n");

		if (message.getDirection () == MessageDirection.in) {

			printFormat (
				"<tr>\n",
				"<th>Number from</th>\n",

				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getNumber ()),

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
				"<tr>\n",
				"<th>Number to</th>\n",
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getNumber ()),
				"</tr>\n");

		}

		printFormat (
			"<tr> <th>Status</th> <td>%h</td> </tr>\n",
			message.getStatus ().getDescription ());

		printFormat (
			"<tr> <th>Direction</th> <td>%h</td> </tr>\n",
			message.getDirection ());

		printFormat (
			"<tr>\n",
			"<th>Route</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				message.getRoute ()),
			"</tr>\n");

		printFormat (
			"<tr> <th>Network</th> <td>%h</td> </tr>\n",
			message.getNetwork ().getDescription ());

		printFormat (
			"<tr> <th>Service</th> %s </tr>\n",
			objectManager.tdForObjectMiniLink (
				message.getService ()));

		printFormat (
			"<tr> <th>Affiliate</th> %s </tr>\n",
			objectManager.tdForObjectMiniLink (
				message.getAffiliate ()));

		if (message.getDirection () == MessageDirection.in) {

			printFormat (
				"<tr>\n",
				"<th>Time sent</th>\n",
				"<td>%h</td>\n",
				message.getNetworkTime () != null
					? userConsoleLogic.timestampWithTimezoneString (
						message.getNetworkTime ())
					: "-",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time received</th>\n",
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time processed</th>\n",
				"<td>%h</td>\n",
				message.getProcessedTime () != null
					? userConsoleLogic.timestampWithTimezoneString (
						message.getProcessedTime ())
					: "-",
				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<th>Command</th>\n",

				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getCommand ()),

				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Time created</th>\n",
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time sent</th>\n",
				"<td>%h</td>\n",
				message.getProcessedTime () != null
					? userConsoleLogic.timestampWithTimezoneString (
						message.getProcessedTime ())
					: "-",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Time received</th>\n",

				"<td>%h</td>\n",
				message.getNetworkTime () != null
					? userConsoleLogic.timestampWithTimezoneString (
						message.getNetworkTime ())
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

		if (
			isPresent (
				failedMessage)
		) {

			printFormat (
				"<tr>\n",
				"<th>Failure reason</th>\n",
				"<td>%h</td>",
				failedMessage.get ().getError (),
				"</tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				message.getUser ()),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Delivery type</th>",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				message.getDeliveryType ()),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
