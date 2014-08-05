package wbs.sms.object.messages;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.Date;
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
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.console.MessageConsoleStuff;
import wbs.sms.message.core.console.MessageSource;
import wbs.sms.message.core.model.MessageRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectSmsMessagesPart")
public
class ObjectSmsMessagesPart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	TimeFormatter timeFormatter;

	@Getter @Setter
	String localName;

	@Getter @Setter
	MessageSource messageSource;

	TabList.Prepared viewTabsPrepared;

	ViewMode viewMode;
	ObsoleteDateField dateField;

	List<MessageRec> messages;

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

		requestContext.request ("date", dateField.text);

		// get start and end dates

		Calendar cal = Calendar.getInstance ();
		cal.setTime (dateField.date);
		Date startDate = cal.getTime ();
		cal.add (Calendar.DATE, 1);
		Date endDate = cal.getTime ();

		// do the query

		messages = messageSource.findMessages (
			startDate,
			endDate,
			viewMode.viewMode);

	}

	// =============================================================== body

	@Override
	public
	void goBodyStuff () {

		viewTabsPrepared.go (
			requestContext);

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
			"<p>Date<br>\n");

		printFormat (
			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		if (messages == null)
			return;

		ObsoleteDateLinks.dailyBrowserParagraph (
			out,
			localUrl,
			requestContext.getFormData (),
			dateField.date);

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

		Calendar cal =
			Calendar.getInstance ();

		int dayNumber = 0;

		for (MessageRec message : messages) {

			cal.setTime (message.getCreatedTime ());

			int newDayNumber =
				+ (cal.get (Calendar.YEAR) << 9)
				+ cal.get (Calendar.DAY_OF_YEAR);

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
				int index = 0;
				index < medias.size ();
				index ++
			) {

				printFormat (

					"<a href=\"%h\">",
					requestContext.resolveApplicationUrl (
						stringFormat (
							"/message",
							"/%u",
							message.getId (),
							"/media_details",
							"?i=%u",
							index)),

					"<img src=\"%h\">",
					requestContext.resolveApplicationUrl (
						stringFormat (
							"/message",
							"/%u",
							message.getId (),
							"/media",
							"?thumb=32",
							"&i=%u",
							index)),

					"</a>");

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
