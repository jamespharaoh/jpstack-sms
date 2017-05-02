package wbs.sms.magicnumber.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("magicNumberUpdatePart")
public
class MagicNumberUpdatePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

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

			// form open

			htmlFormOpenPost ();

			// numbers

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"Numbers<br>");

			formatWriter.writeFormat (
				"<textarea",
				" name=\"numbers\"",
				" rows=\"16\"",
				" cols=\"40\"",
				">%h</textarea>",
				requestContext.parameterOrEmptyString (
					"numbers"));

			htmlParagraphClose ();

			// form controls

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"create\"",
				" value=\"create magic numbers\"",
				">");

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"delete\"",
				" value=\"delete magic numbers\"",
				">");

			htmlParagraphClose ();

			// form close

			htmlFormClose ();

		}

	}

}
