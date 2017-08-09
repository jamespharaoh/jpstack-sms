package wbs.sms.number.list.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.list.model.NumberListRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberUpdatePart")
public
class NumberListNumberUpdatePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberListConsoleHelper numberListHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	NumberListRec numberList;

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

			numberList =
				numberListHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			goDetails (
				transaction,
				formatWriter);

			goForm (
				transaction,
				formatWriter);

		}

	}

	void goDetails (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goDetails");

		) {

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"Numbers",
				integerToDecimalString (
					numberList.getNumberCount ()));

			htmlTableClose (
				formatWriter);

		}

	}

	void goForm (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goForm");

		) {

			// form open

			htmlFormOpenPost (
				formatWriter);

			// numbers

			htmlParagraphOpen (
				formatWriter);

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

			htmlParagraphClose (
				formatWriter);

			// form controls

			htmlParagraphOpen (
				formatWriter);

			if (
				privChecker.canRecursive (
					transaction,
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
					transaction,
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

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}
