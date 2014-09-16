package wbs.sms.number.list.console;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;
import wbs.sms.number.list.model.NumberListRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberUpdatePart")
public
class NumberListNumberUpdatePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	NumberListConsoleHelper numberListHelper;

	@Inject
	PrivChecker privChecker;

	// state

	NumberListRec numberList;

	// implementation

	@Override
	public
	void prepare () {

		numberList =
			numberListHelper.find (
				requestContext.stuffInt ("numberListId"));

	}

	@Override
	public
	void goBodyStuff () {

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
			numberList.getNumberCount (),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

	void goForm () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p>Numbers<br>\n",

			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea></p>\n",
			requestContext.parameter ("numbers"));

		printFormat (
			"<p>\n");

		if (privChecker.can (
				numberList,
				"number_list_add")) {

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"add\"",
				" value=\"add numbers\"",
				">\n");

		}

		if (privChecker.can (
				numberList,
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
