package wbs.sms.object.messages;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.HtmlTableCellWriter;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.console.MessageSource;
import wbs.sms.message.core.model.MessageRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectSmsMessagesPart")
public
class ObjectSmsMessagesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <TabList> tabListProvider;

	// properties

	@Getter @Setter
	String localName;

	@Getter @Setter
	MessageSource messageSource;

	// state

	TabList.Prepared viewTabsPrepared;

	ViewMode viewMode;

	ObsoleteDateField dateField;

	List <MessageRec> messages;

	ViewMode defaultViewMode;

	Map <String, ViewMode> viewModesByName =
		new HashMap<> ();

	TabList viewTabs;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			viewTabs =
				tabListProvider.get ();

			defaultViewMode =
				addViewMode (
					"all",
					"All",
					MessageSource.ViewMode.all);

			addViewMode (
				"in",
				"In",
				MessageSource.ViewMode.in);

			addViewMode (
				"out",
				"Out",
				MessageSource.ViewMode.out);

			addViewMode (
				"unknown",
				"Unknown",
				MessageSource.ViewMode.sent);

			addViewMode (
				"success",
				"Success",
				MessageSource.ViewMode.delivered);

			addViewMode (
				"failed",
				"Failed",
				MessageSource.ViewMode.undelivered);

		}

	}

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

			requestContext.request (
				"localName",
				localName);

			// work out view mode and setup tabs

			viewMode =
				viewModesByName.get (
					requestContext.parameterOrNull (
						"view"));

			if (viewMode == null) {

				viewMode =
					defaultViewMode;

			}

			viewTabsPrepared =
				viewTabs.prepare (
					transaction,
					viewMode.viewTab);

			// get date

			dateField =
				ObsoleteDateField.parse (
					requestContext.parameterOrNull (
						"date"));

			if (dateField.date == null) {

				requestContext.addError (
					"Invalid date");

				return;

			}

			requestContext.request (
				"date",
				dateField.text);

			// do the query

			messages =
				messageSource.findMessages (
					transaction,
					dateField.date.toInterval (),
					viewMode.viewMode);

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

			/*
			viewTabsPrepared.go (
				requestContext);
			*/

			String localUrl =
				requestContext.resolveLocalUrl (
					localName);

			htmlFormOpenGetAction (
				formatWriter,
				localUrl);

			formatWriter.writeLineFormat (
				"<p",
				" class=\"links\"",
				">");

			formatWriter.increaseIndent ();

			formatWriter.writeLineFormat (
				"Date");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"date\"",
				" value=\"%h\"",
				dateField.text,
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"ok\"",
				">");

			ObsoleteDateLinks.dailyBrowserLinks (
				formatWriter,
				localUrl,
				requestContext.formData (),
				dateField.date);

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"</p>");

			htmlFormClose (
				formatWriter);

			if (
				isNull (
					messages)
			) {
				return;
			}

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Time",
				"From",
				"To",
				"Route",
				"Id",
				"Status",
				"Media");

			Calendar calendar =
				Calendar.getInstance ();

			int dayNumber = 0;

			for (
				MessageRec message
					: messages
			) {

				calendar.setTime (
					instantToDateNullSafe (
						message.getCreatedTime ()));

				int newDayNumber =
					+ (calendar.get (Calendar.YEAR) << 9)
					+ calendar.get (Calendar.DAY_OF_YEAR);

				if (newDayNumber != dayNumber) {

					htmlTableRowSeparatorWrite (
						formatWriter);

					htmlTableRowOpen (
						formatWriter,
						htmlAttribute (
							"style",
							"font-weight: bold"));

					htmlTableCellWrite (
						formatWriter,
						userConsoleLogic.dateStringLong (
							transaction,
							message.getCreatedTime ()),
						htmlColumnSpanAttribute (7l));

					htmlTableRowClose (
						formatWriter);

					dayNumber =
						newDayNumber;

				}

				String rowClass =
					messageConsoleLogic.classForMessage (
						message);

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				htmlTableCellWrite (
					formatWriter,
					userConsoleLogic.timeString (
						transaction,
						message.getCreatedTime ()));

				htmlTableCellWrite (
					formatWriter,
					message.getNumFrom ());

				htmlTableCellWrite (
					formatWriter,
					message.getNumTo ());

				htmlTableCellWrite (
					formatWriter,
					message.getRoute ().getCode ());

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						message.getId ()));

				messageConsoleLogic.writeTdForMessageStatus (
					transaction,
					formatWriter,
					message.getStatus ());

				List <MediaRec> medias =
					message.getMedias ();

				htmlTableCellOpen (
					formatWriter,
					htmlRowSpanAttribute (2l));

				for (
					MediaRec media
						: medias
				) {

					if (media.getThumb32Content () == null)
						continue;

					mediaConsoleLogic.writeMediaThumb32 (
						transaction,
						formatWriter,
						media);

				}

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				new HtmlTableCellWriter ()

					.href (
						consoleObjectManager.localLink (
							transaction,
							message))

					.columnSpan (
						6l)

					.write (
						formatWriter);

				formatWriter.writeFormat (
					"%h",
					message.getText ().getText ());

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

	// private implementation

	private
	ViewMode addViewMode (
			@NonNull String name,
			@NonNull String label,
			@NonNull MessageSource.ViewMode viewMode) {

		ViewTab viewTab =
			new ViewTab (
				label,
				name);

		viewTabs.add (
			viewTab);

		ViewMode newViewMode =
			new ViewMode (
				name,
				label,
				viewMode,
				viewTab);

		viewModesByName.put (
			name,
			newViewMode);

		return newViewMode;

	}

	// view tab

	private
	class ViewTab
		extends Tab {

		@SuppressWarnings ("unused")
		private final
		String name;

		private
		ViewTab (
				String newLabel,
				String newName) {

			super (newLabel);

			name = newName;

		}

		@Override
		public
		String getUrl (
				@NonNull Transaction parentTransaction) {

			return stringFormat (
				"%s",
				requestContext.resolveLocalUrl (
					localName),
				"?date=%u",
				optionalOr (
					genericCastUnchecked (
						requestContext.request (
							"date")),
					""));

		}

	}

	// view mode

	private static
	class ViewMode {

		@SuppressWarnings ("unused")
		private
		String name, label;

		private
		MessageSource.ViewMode viewMode;

		private
		ViewTab viewTab;

		private
		ViewMode (
				String newName,
				String newLabel,
				MessageSource.ViewMode newViewMode,
				ViewTab newViewTab) {

			name = newName;
			label = newLabel;
			viewMode = newViewMode;
			viewTab = newViewTab;

		}

	}

}
