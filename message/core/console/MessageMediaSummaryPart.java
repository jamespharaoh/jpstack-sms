package wbs.sms.message.core.console;

import static wbs.utils.etc.Misc.prettySize;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.awt.image.BufferedImage;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;

import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageMediaSummaryPart")
public
class MessageMediaSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	// state

	MediaRec media;
	Optional <BufferedImage> imageOptional;
	int mediaIndex;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			MessageRec message =
				messageHelper.findFromContextRequired (
					transaction);

			mediaIndex =
				Integer.parseInt (
					requestContext.parameterRequired (
						"index"));

			media =
				message.getMedias ().get (
					mediaIndex);

			imageOptional =
				mediaLogic.getImage (
					transaction,
					media);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
				optionalIsPresent (
					imageOptional)
			) {

				BufferedImage image =
					optionalGetRequired (
						imageOptional);

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
					transaction,
					media,
					600,
					600));

			htmlTableClose ();

		}

	}

}
