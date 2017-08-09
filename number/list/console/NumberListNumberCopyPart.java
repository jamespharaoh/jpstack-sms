package wbs.sms.number.list.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.list.model.NumberListRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberCopyPart")
public
class NumberListNumberCopyPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberListConsoleHelper numberListHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	List <NumberListRec> browseableNumberLists;

	NumberListRec thisNumberList;

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

			// this number list

			thisNumberList =
				numberListHelper.findFromContextRequired (
					transaction);

			// browseable number lists

			List <NumberListRec> allNumberLists =
				numberListHelper.findAll (
					transaction);

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
						transaction,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goDetails");

		) {

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"Numbers",
				integerToDecimalString (
					thisNumberList.getNumberCount ()));

			htmlTableClose (
				formatWriter);

		}

	}

	void goForm (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goForm");

		) {

			// form open

			htmlFormOpenPost (
				formatWriter);

			// number list

			htmlParagraphOpen (
				formatWriter);

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

			htmlParagraphClose (
				formatWriter);

			// controls

			htmlParagraphOpen (
				formatWriter);

			if (
				privChecker.canRecursive (
					taskLogger,
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
					taskLogger,
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

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}
