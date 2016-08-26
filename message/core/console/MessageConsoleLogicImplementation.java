package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.CollectionUtils.collectionIsNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsNotEmpty;
import static wbs.framework.utils.etc.StringUtils.spacify;

import javax.inject.Inject;

import lombok.NonNull;

import org.apache.commons.io.output.StringBuilderWriter;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.formatwriter.FormatWriter;
import wbs.framework.utils.formatwriter.WriterFormatWriter;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@SingletonComponent ("messageConsoleLogic")
public
class MessageConsoleLogicImplementation
	implements MessageConsoleLogic {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageConsolePluginManager messageConsolePluginManager;

	@Inject
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	String messageContentText (
			@NonNull MessageRec message) {

		MessageConsolePlugin messageConsolePlugin =
			messageConsolePluginManager.getPlugin (
				message.getMessageType ().getCode ());

		if (messageConsolePlugin != null) {

			return messageConsolePlugin.messageSummaryText (
				message);

		}

		return message.getText ().getText ();

	}

	@Override
	public
	String messageContentHtml (
			@NonNull MessageRec message) {

		MessageConsolePlugin messageConsolePlugin =
			messageConsolePluginManager.getPlugin (
				message.getMessageType ().getCode ());

		if (messageConsolePlugin != null) {

			return messageConsolePlugin.messageSummaryHtml (
				message);

		}

		StringBuilderWriter stringWriter =
			new StringBuilderWriter ();

		FormatWriter formatWriter =
			new WriterFormatWriter (
				stringWriter);

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

					formatWriter.writeFormat (
						"%s\n",
						mediaConsoleLogic.mediaThumb32OrText (
							media));

				} else {

					formatWriter.writeFormat (
						"<a href=\"%h\">%s</a>\n",

						requestContext.resolveContextUrl (
							stringFormat (
								"/message",
								"/%u",
								message.getId (),
								"/message_mediaSummary",
								"?index=%u",
								index ++)),

						mediaConsoleLogic.mediaThumb32OrText (
							media));

				}

			}

		} else {

			formatWriter.writeFormat (
				"%h",
				spacify (
					message.getText ().getText ()));

		}

		return stringWriter.toString ();

	}

	@Override
	public
	String tdForMessageStatus (
			@NonNull MessageStatus messageStatus) {

		return stringFormat (
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
