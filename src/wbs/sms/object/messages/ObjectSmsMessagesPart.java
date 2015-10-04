package wbs.sms.object.messages;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.html.ObsoleteDateLinks;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.tab.Tab;
import wbs.platform.console.tab.TabList;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.console.MessageConsoleStuff;
import wbs.sms.message.core.console.MessageSource;
import wbs.sms.message.core.model.MessageRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectSmsMessagesPart")
public
class ObjectSmsMessagesPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String localName;

	@Getter @Setter
	MessageSource messageSource;

	// state

	TabList.Prepared viewTabsPrepared;

	ViewMode viewMode;
	ObsoleteDateField dateField;

	List<MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		requestContext.request (
			"localName",
			localName);

		// work out view mode and setup tabs

		viewMode =
			viewModesByName.get (
				requestContext.parameter ("view"));

		if (viewMode == null)
			viewMode = defaultViewMode;

		viewTabsPrepared =
			viewTabs.prepare (
				viewMode.viewTab);

		// get date

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

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
				dateField.date.toInterval (),
				viewMode.viewMode);

	}

	// =============================================================== body

	@Override
	public
	void renderHtmlBodyContent () {

		/*
		viewTabsPrepared.go (
			requestContext);
		*/

		String localUrl =
			requestContext.resolveLocalUrl (
				localName);

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"%h\"",
			localUrl,
			">\n");

		printFormat (
			"<p",
			" class=\"links\"",
			">Date\n",

			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">\n",

			"%s</p>\n",
			ObsoleteDateLinks.dailyBrowserLinks (
				localUrl,
				requestContext.getFormData (),
				dateField.date));

		printFormat (
			"</form>\n");

		if (messages == null)
			return;

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Time</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Route</th>\n",
			"<th>Id</th>\n",
			"<th>Status</th>\n",
			"<th>Media</th>\n",
			"</tr>\n");

		Calendar calendar =
			Calendar.getInstance ();

		int dayNumber = 0;

		for (MessageRec message : messages) {

			calendar.setTime (
				message.getCreatedTime ());

			int newDayNumber =
				+ (calendar.get (Calendar.YEAR) << 9)
				+ calendar.get (Calendar.DAY_OF_YEAR);

			if (newDayNumber != dayNumber) {

				printFormat (
					"<tr class=\"sep\">\n",

					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"7\">%h</td>\n",
					timeFormatter.instantToDateStringLong (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							message.getCreatedTime ())),

					"</tr>\n");

				dayNumber =
					newDayNumber;

			}

			String rowClass =
				MessageConsoleStuff.classForMessage (message);

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getCreatedTime ())),

				"<td>%h</td>\n",
				message.getNumFrom (),

				"<td>%h</td>\n",
				message.getNumTo (),

				"<td>%h</td>\n",
				message.getRoute ().getCode (),

				"<td>%h</td>\n",
				message.getId (),

				"%s\n",
				MessageConsoleStuff.tdForMessageStatus (
					message.getStatus ()));

			List<MediaRec> medias =
				message.getMedias ();

			printFormat (
				"<td rowspan=\"2\">");

			for (
				MediaRec media
					: medias
			) {

				if (media.getThumb32Content () == null)
					continue;

				printFormat (
					"%s\n",
					mediaConsoleLogic.mediaThumb32 (
						media));

			}

			printFormat (
				"</td>\n");

			printFormat (
				"</tr>\n");

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"%s%h</td>\n",
				Html.magicTd (
					consoleObjectManager.localLink (message),
					null,
					6),
				message.getText (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

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
		String getUrl () {

			return stringFormat (
				"%s",
				requestContext.resolveLocalUrl (
					localName),
				"?date=%u",
				requestContext.request (
					"date"));

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
