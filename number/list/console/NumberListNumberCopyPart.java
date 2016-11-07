package wbs.sms.number.list.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.number.list.model.NumberListRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberCopyPart")
public
class NumberListNumberCopyPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	NumberListConsoleHelper numberListHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	List <NumberListRec> browseableNumberLists;

	NumberListRec thisNumberList;

	// implementation

	@Override
	public
	void prepare () {

		// this number list

		thisNumberList =
			numberListHelper.findRequired (
				requestContext.stuffInteger (
					"numberListId"));

		// browseable number lists

		List <NumberListRec> allNumberLists =
			numberListHelper.findAll ();

		ImmutableList.Builder <NumberListRec> browseableNumberListsBuilder =
			ImmutableList.builder ();

		for (
			NumberListRec someNumberList
				: allNumberLists
		) {

			if (someNumberList == thisNumberList)
				continue;

			if (
				! privChecker.canRecursive (
					someNumberList,
					"number_list_browse")
			) {
				continue;
			}

			browseableNumberListsBuilder.add (
				someNumberList);

		}

		browseableNumberLists =
			browseableNumberListsBuilder.build ();

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
				thisNumberList.getNumberCount ()));

		htmlTableClose ();

	}

	void goForm () {

		// form open

		htmlFormOpenPost ();

		// number list

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Number list<br>");

		formatWriter.writeLineFormat (
			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea>",
			requestContext.parameterOrEmptyString (
				"numbers"));

		htmlParagraphClose ();

		// controls

		htmlParagraphOpen ();

		if (
			privChecker.canRecursive (
				thisNumberList,
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
				thisNumberList,
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
