package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.prettySize;

import java.awt.image.BufferedImage;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageMediaSummaryPart")
public
class MessageMediaSummaryPart
	extends AbstractPagePart {

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageObjectHelper messageHelper;

	MediaRec media;
	BufferedImage image;
	int mediaIndex;

	@Override
	public
	void prepare () {

		MessageRec message =
			messageHelper.find (
				requestContext.stuffInt ("messageId"));

		mediaIndex =
			Integer.parseInt (
				requestContext.parameter ("index"));

		media =
			message.getMedias ().get (mediaIndex);

		image =
			mediaLogic.getImage (media);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Filename</th>\n",
			"<td>%h</td>\n",
			media.getFilename (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Type</th>\n",
			"<td>%h</td>\n",
			media.getMediaType ().getMimeType (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Size</th>\n",
			"<td>%h</td>\n",
			prettySize (
				media.getContent ().getData ().length),
			"</tr>\n");

		if (image != null) {

			printFormat (
				"<tr>\n",
				"<th>Width</th>\n",
				"<td>%h</td>\n",
				image.getWidth (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Height</th>\n",
				"<td>%h</td>\n",
				image.getHeight (),
				"</tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>Content</th>\n",
			"<td>%s</td>\n",
			mediaConsoleLogic.mediaContentScaled (
				media,
				600,
				600),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
