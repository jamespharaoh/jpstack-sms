package wbs.sms.object.messages;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.instantToDateNullSafe;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ObsoleteDateField;
import wbs.console.html.ObsoleteDateLinks;
import wbs.console.part.AbstractPagePart;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
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

	// dependencies

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
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
				requestContext.parameterOrNull ("view"));

		if (viewMode == null)
			viewMode = defaultViewMode;

		viewTabsPrepared =
			viewTabs.prepare (
				viewMode.viewTab);

		// get date

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameterOrNull ("date"));

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

				printFormat (
					"<tr class=\"sep\">\n",

					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"7\">%h</td>\n",
					userConsoleLogic.dateStringLong (
						message.getCreatedTime ()),

					"</tr>\n");

				dayNumber =
					newDayNumber;

			}

			String rowClass =
				messageConsoleLogic.classForMessage (
					message);

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass);

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timeString (
					message.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				message.getNumFrom ());

			printFormat (
				"<td>%h</td>\n",
				message.getNumTo ());

			printFormat (
				"<td>%h</td>\n",
				message.getRoute ().getCode ());

			printFormat (
				"<td>%h</td>\n",
				message.getId ());

			printFormat (
				"%s\n",
				messageConsoleLogic.tdForMessageStatus (
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
				ifNull (
					requestContext.request (
						"date"),
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
