package wbs.sms.message.core.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.bytesToString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.utils.web.HtmlTableCellWriter;

@PrototypeComponent ("messageThreadPart")
public
class MessageThreadPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	Set <MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		MessageRec message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		messages =
			new TreeSet<> (
				messageHelper.findByThreadId (
					message.getThreadId ()));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"ID",
			"From",
			"To",
			"Time",
			"Route",
			"Status",
			"Media");

		for (
			MessageRec message
				: messages
		) {

			// separator

			htmlTableRowSeparatorWrite ();

			// various fields

			String rowClass =
				messageConsoleLogic.classForMessage (
					message);

			htmlTableRowOpen (
				htmlClassAttribute (
					rowClass));

			htmlTableCellWrite (
				integerToDecimalString (
					message.getId ()));

			htmlTableCellWrite (
				message.getNumFrom ());

			htmlTableCellWrite (
				message.getNumTo ());

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()));

			htmlTableCellWrite (
				message.getRoute ().getCode ());

			messageConsoleLogic.writeTdForMessageStatus (
				formatWriter,
				message.getStatus ());

			List <MediaRec> medias =
				message.getMedias ();

			htmlTableCellOpen (
				htmlRowSpanAttribute (2l));

			for (
				int index = 0;
				index < medias.size ();
				index++
			) {

				MediaRec media =
					medias.get (index);

				if (
					stringEqualSafe (
						media.getMediaType ().getMimeType (),
						"text/plain")
				) {

					formatWriter.writeLineFormat (
						"%h",
						bytesToString (
							media.getContent ().getData (),
							media.getEncoding ()));

				} else {

					mediaConsoleLogic.writeMediaThumb32OrText (
						media);

				}

			}

			htmlTableCellClose ();

			htmlTableRowClose ();

			// message

			htmlTableRowOpen (
				htmlClassAttribute (
					rowClass));

			new HtmlTableCellWriter ()

				.href (
					requestContext.resolveContextUrl (
						stringFormat (
							"/message",
							"/%u",
							message.getId (),
							"/message_summary")))

				.columnSpan (
					6l)

				.write (
					formatWriter);

			messageConsoleLogic.writeMessageContentHtml (
				formatWriter,
				message);

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
