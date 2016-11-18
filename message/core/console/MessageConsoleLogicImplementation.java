package wbs.sms.message.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.string.StringUtils.stringFormatObsolete;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.web.utils.HtmlUtils.htmlLinkWriteHtml;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.utils.string.FormatWriter;

@SingletonComponent ("messageConsoleLogic")
public
class MessageConsoleLogicImplementation
	implements MessageConsoleLogic {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageConsolePluginManager messageConsolePluginManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void writeMessageContentText (
			@NonNull FormatWriter formatWriter,
			@NonNull MessageRec message) {

		MessageConsolePlugin messageConsolePlugin =
			messageConsolePluginManager.getPlugin (
				message.getMessageType ().getCode ());

		if (
			isNotNull (
				messageConsolePlugin)
		) {

			messageConsolePlugin.writeMessageSummaryText (
				formatWriter,
				message);

		} else {

			formatWriter.writeString (
				message.getText ().getText ());

		}

	}

	@Override
	public
	void writeMessageContentHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull MessageRec message) {

		MessageConsolePlugin messageConsolePlugin =
			messageConsolePluginManager.getPlugin (
				message.getMessageType ().getCode ());

		if (messageConsolePlugin != null) {

			messageConsolePlugin.writeMessageSummaryHtml (
				formatWriter,
				message);

			return;

		}

		if (
			collectionIsNotEmpty (
				message.getMedias ())
		) {

			if (

				isNotNull (
					message.getSubjectText ())

				&& stringIsNotEmpty (
					message.getSubjectText ().getText ())

			) {

				formatWriter.writeFormat (
					"%h:\n",
					message.getSubjectText ().getText ());

			}

			int index = 0;

			for (
				MediaRec media
					: message.getMedias ()
			) {

				if (
					mediaLogic.isText (
						media.getMediaType ().getMimeType ())
				) {

					mediaConsoleLogic.writeMediaThumb32OrText (
						formatWriter,
						media);

				} else {

					htmlLinkWriteHtml (
						formatWriter,
						requestContext.resolveContextUrl (
							stringFormatObsolete (
								"/message",
								"/%u",
								message.getId (),
								"/message_mediaSummary",
								"?index=%u",
								index ++)),
						() -> mediaConsoleLogic.writeMediaThumb32OrText (
							formatWriter,
							media));

				}

			}

		} else {

			formatWriter.writeFormat (
				"%h",
				spacify (
					message.getText ().getText ()));

		}

	}

	@Override
	public
	void writeTdForMessageStatus (
			@NonNull FormatWriter formatWriter,
			@NonNull MessageStatus messageStatus) {

		formatWriter.writeLineFormat (
			"<td class=\"%h\">%h</td>",
			classForMessageStatus (
				messageStatus),
			textForMessageStatus (
				messageStatus));

	}

	@Override
	public
	String classForMessage (
			@NonNull MessageRec message) {

		if (message.getDirection () == MessageDirection.in) {

			return "message-in";

		} else if (message.getCharge () > 0) {

			return "message-out-charge";

		} else {

			return "message-out";

		}

	}

	@Deprecated
	public
	String textForMessageStatus (
			@NonNull MessageStatus messageStatus) {

		return messageStatus.getDescription ();

	}

	@Override
	public
	String classForMessageStatus (
			@NonNull MessageStatus messageStatus) {

		if (messageStatus.isGoodType ())
			return "message-status-succeeded";

		if (messageStatus.isBadType ())
			return "message-status-failed";

		return "message-status-unknown";

	}

	@Override
	public
	String classForMessageDirection (
			@NonNull MessageDirection direction) {

		switch (direction) {

		case in:
			return "message-in";

		case out:
			return "message-out";

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	char charForMessageStatus (
			@NonNull MessageStatus messageStatus) {

		switch (messageStatus) {

		// pending status

		case held:
			return 'h';

		case pending:
			return ' ';

		case sent:
			return '-';

		case submitted:
			return '+';

		// successful status

		case processed:
			return 'p';

		case delivered:
			return 'd';

		// failure status

		case cancelled:
			return 'c';

		case failed:
			return 'f';

		case rejected:
			return 'r';

		case undelivered:
			return 'u';

		case notProcessed:
			return 'n';

		case ignored:
			return 'i';

		case manuallyDelivered:
		case manuallyProcessed:
		case manuallyUndelivered:
			return 'm';

		case reportTimedOut:
			return '?';

		case blacklisted:
			return 'b';

		}

		throw new IllegalArgumentException (
			"Unknown message status");

	}

	@Deprecated
	public
	String textForMessageDirection (
			@NonNull MessageDirection direction) {

		switch (direction) {

		case in:
			return "in";

		case out:
			return "out";

		}

		throw new IllegalArgumentException (
			"Invalid direction " + direction);

	}

}
