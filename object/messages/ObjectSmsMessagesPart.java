package wbs.sms.object.messages;

import static wbs.utils.etc.Misc.isNull;
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

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.HtmlTableCellWriter;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.part.AbstractPagePart;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.console.MessageSource;
import wbs.sms.message.core.model.MessageRec;

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
	UserConsoleLogic userConsoleLogic;

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

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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

			if (viewMode == null)
				viewMode = defaultViewMode;

			viewTabsPrepared =
				viewTabs.prepare (
					taskLogger,
					viewMode.viewTab);

			// get date

			dateField =
				ObsoleteDateField.parse (
					requestContext.parameterOrNull (
						"date"));

			if (dateField.date == null) {
				requestContext.addError ("Invalid date");
				return;
			}

			requestContext.request (
				"date",
				dateField.text);

			// do the query

			messages =
				messageSource.findMessages (
					taskLogger,
					dateField.date.toInterval (),
					viewMode.viewMode);

		}

	}

	// =============================================================== body

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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

			htmlFormClose ();

			if (
				isNull (
					messages)
			) {
				return;
			}

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
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

					htmlTableRowSeparatorWrite ();

					htmlTableRowOpen (
						htmlAttribute (
							"style",
							"font-weight: bold"));

					htmlTableCellWrite (
						userConsoleLogic.dateStringLong (
							message.getCreatedTime ()),
						htmlColumnSpanAttribute (7l));

					htmlTableRowClose ();

					dayNumber =
						newDayNumber;

				}

				String rowClass =
					messageConsoleLogic.classForMessage (
						message);

				htmlTableRowSeparatorWrite ();

				htmlTableRowOpen (
					htmlClassAttribute (
						rowClass));

				htmlTableCellWrite (
					userConsoleLogic.timeString (
						message.getCreatedTime ()));

				htmlTableCellWrite (
					message.getNumFrom ());

				htmlTableCellWrite (
					message.getNumTo ());

				htmlTableCellWrite (
					message.getRoute ().getCode ());

				htmlTableCellWrite (
					integerToDecimalString (
						message.getId ()));

				messageConsoleLogic.writeTdForMessageStatus (
					formatWriter,
					message.getStatus ());

				List <MediaRec> medias =
					message.getMedias ();

				htmlTableCellOpen (
					htmlRowSpanAttribute (2l));

				for (
					MediaRec media
						: medias
				) {

					if (media.getThumb32Content () == null)
						continue;

					mediaConsoleLogic.writeMediaThumb32 (
						taskLogger,
						formatWriter,
						media);

				}

				htmlTableCellClose ();

				htmlTableRowClose ();

				htmlTableRowOpen (
					htmlClassAttribute (
						rowClass));

				new HtmlTableCellWriter ()

					.href (
						consoleObjectManager.localLink (
							taskLogger,
							message))

					.columnSpan (
						6l)

					.write (
						formatWriter);

				formatWriter.writeFormat (
					"%h",
					message.getText ().getText ());

				htmlTableCellClose ();

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

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
				@NonNull TaskLogger parentTaskLogger) {

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

	private final
	ViewMode defaultViewMode;

	private final
	Map<String,ViewMode> viewModesByName =
		new HashMap<String,ViewMode> ();

	private final
	TabList viewTabs =
		new TabList ();;

	private
	ViewMode addViewMode (
			String name,
			String label,
			MessageSource.ViewMode viewMode) {

		ViewTab viewTab =
			new ViewTab (label, name);

		viewTabs.add (viewTab);

		ViewMode newViewMode =
			new ViewMode (name, label, viewMode, viewTab);

		viewModesByName.put (name, newViewMode);

		return newViewMode;

	}

	{

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
