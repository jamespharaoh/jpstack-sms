package wbs.sms.number.list.console;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import lombok.experimental.Accessors;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.number.list.model.NumberListRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberCopyPart")
public
class NumberListNumberCopyPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	NumberListConsoleHelper numberListHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	// state

	List<NumberListRec> browseableNumberLists;

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

		List<NumberListRec> allNumberLists =
			numberListHelper.findAll ();

		ImmutableList.Builder<NumberListRec> browseableNumberListsBuilder =
			ImmutableList.<NumberListRec>builder ();

		for (NumberListRec someNumberList
				: allNumberLists) {

			if (someNumberList == thisNumberList)
				continue;

			if (! privChecker.canRecursive (
					someNumberList,
					"number_list_browse"))
				continue;

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

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Numbers</th>\n",
			"<td>%h</td>\n",
			thisNumberList.getNumberCount (),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

	void goForm () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p>Number list<br>\n",

			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea></p>\n",
			requestContext.parameterOrEmptyString (
				"numbers"));

		printFormat (
			"<p>\n");

		if (privChecker.canRecursive (
				thisNumberList,
				"number_list_add")) {

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"add\"",
				" value=\"add numbers\"",
				">\n");

		}

		if (privChecker.canRecursive (
				thisNumberList,
				"number_list_remove")) {

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"remove\"",
				" value=\"remove numbers\"",
				">\n");

		}

		printFormat (
			"</p>\n");

		printFormat (
			"</form>\n");

	}

}
