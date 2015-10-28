package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.spacify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import org.apache.commons.io.output.StringBuilderWriter;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageRec;

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

		return stringFormat (
			"\"%h\"",
			spacify (
				message.getText ().getText ()));

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
			new FormatWriterWriter (
				stringWriter);

		if (
			isNotEmpty (
				message.getMedias ())
		) {

			if (

				isNotNull (
					message.getSubjectText ())

				&& isNotEmpty (
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

}
