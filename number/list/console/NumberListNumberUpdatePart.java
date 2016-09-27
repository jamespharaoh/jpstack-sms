package wbs.sms.number.list.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.number.list.model.NumberListRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberUpdatePart")
public
class NumberListNumberUpdatePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	NumberListConsoleHelper numberListHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	NumberListRec numberList;

	// implementation

	@Override
	public
	void prepare () {

		numberList =
			numberListHelper.findRequired (
				requestContext.stuffInteger (
					"numberListId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		goDetails ();

		goForm ();

	}

	void goDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Numbers",
			integerToDecimalString (
				numberList.getNumberCount ()));

		htmlTableClose ();

	}

	void goForm () {

		// form open

		htmlFormOpenPost ();

		// numbers

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Numbers<br>");

		formatWriter.writeLineFormat (
			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea>",
			requestContext.parameterOrEmptyString (
				"numbers"));

		htmlParagraphClose ();

		// form controls

		htmlParagraphOpen ();

		if (
			privChecker.canRecursive (
				numberList,
				"number_list_add")
		) {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"add\"",
				" value=\"add numbers\"",
				">");

		}

		if (
			privChecker.canRecursive (
				numberList,
				"number_list_remove")
		) {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"remove\"",
				" value=\"remove numbers\"",
				">");

		}

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
