package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.prettySize;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageMediasPart")
public
class MessageMediasPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MessageObjectHelper messageHelper;

	// state

	MessageRec message;
	List<MediaRec> medias;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.find (
				requestContext.stuffInt ("messageId"));

		medias =
			message.getMedias ();

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Thumbnail</th>\n",
			"<th>Type</th>\n",
			"<th>Filename</th>\n",
			"<th>Size</th>\n");

		if (medias.size () == 0) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"4\">no media</td>\n",
				"</tr>\n");

		} else {

			for (
				int index = 0;
				index < medias.size ();
				index ++
			) {

				MediaRec media =
					medias.get (index);

				printFormat (
					"%s\n",
					Html.magicTr (
						requestContext.resolveLocalUrl (
							stringFormat (
								"/message.mediaSummary",
								"?index=%u",
								index)),
						false));

				printFormat (
					"<td>%s</td>\n",
					mediaConsoleLogic.mediaThumb100 (
						media));

				printFormat (
					"<td>%h</td>\n",
					media.getMediaType ().getMimeType ());

				printFormat (
					"<td>%h</td>\n",
					ifNull (
						media.getFilename (), "-"));

				printFormat (
					"<td>%h</td>\n",
					prettySize (
						media.getContent().getData ().length));

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}