package wbs.sms.message.core.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.bytesToString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

import wbs.console.html.HtmlTableCellWriter;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageThreadPart")
public
class MessageThreadPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			MessageRec message =
				messageHelper.findFromContextRequired (
					transaction);

			messages =
				new TreeSet<> (
					messageHelper.findByThreadId (
						transaction,
						message.getThreadId ()));

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
						transaction,
						message.getCreatedTime ()));

				htmlTableCellWrite (
					message.getRoute ().getCode ());

				messageConsoleLogic.writeTdForMessageStatus (
					transaction,
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
							transaction,
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
						requestContext.resolveContextUrlFormat (
							"/message",
							"/%u",
							integerToDecimalString (
								message.getId ()),
							"/message_summary"))

					.columnSpan (
						6l)

					.write (
						formatWriter);

				messageConsoleLogic.writeMessageContentHtml (
					transaction,
					formatWriter,
					message);

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

}
