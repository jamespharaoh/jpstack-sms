package wbs.sms.message.core.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.prettySize;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.awt.image.BufferedImage;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageMediaSummaryPart")
public
class MessageMediaSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	// state

	MediaRec media;
	BufferedImage image;
	int mediaIndex;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		MessageRec message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		mediaIndex =
			Integer.parseInt (
				requestContext.parameterRequired (
					"index"));

		media =
			message.getMedias ().get (
				mediaIndex);

		image =
			mediaLogic.getImage (
				media);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Filename",
			media.getFilename ());

		htmlTableDetailsRowWrite (
			"Type",
			media.getMediaType ().getMimeType ());

		htmlTableDetailsRowWrite (
			"Size",
			prettySize (
				media.getContent ().getData ().length));

		if (
			isNotNull (
				image)
		) {

			htmlTableDetailsRowWrite (
				"Width",
				integerToDecimalString (
					fromJavaInteger (
						image.getWidth ())));

			htmlTableDetailsRowWrite (
				"Height",
				integerToDecimalString (
					fromJavaInteger (
						image.getHeight ())));

		}

		htmlTableDetailsRowWriteHtml (
			"Content",
			() -> mediaConsoleLogic.writeMediaContentScaled (
				media,
				600,
				600));

		htmlTableClose ();

	}

}
