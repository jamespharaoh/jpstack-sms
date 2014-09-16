package wbs.integrations.dialogue.daemon;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.log4j.Log4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.integrations.dialogue.model.DialogueMmsRouteObjectHelper;
import wbs.integrations.dialogue.model.DialogueMmsRouteRec;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;

@Log4j
@SingletonComponent ("dialogueMmsSender")
public
class DialogueMmsSender
	extends AbstractSmsSender1<DialogueMmsSender.DialogueMmsOutbox> {

	// dependencies

	@Inject
	DialogueMmsRouteObjectHelper dialogueMmsRouteHelper;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "DlgMmsSnd";
	}

	@Override
	protected
	String getSenderCode () {
		return "dialogue_mms";
	}

	// implementation

	@Override
	protected
	DialogueMmsOutbox getMessage (
			OutboxRec outbox)
		throws SendFailureException {

		DialogueMmsOutbox dialogueMmsOutbox =
			new DialogueMmsOutbox ();

		dialogueMmsOutbox.outbox =
			outbox;

		dialogueMmsOutbox.dialogueMmsRoute =
			dialogueMmsRouteHelper.find (
				outbox.getRoute ().getId ());

		if (dialogueMmsOutbox.dialogueMmsRoute == null) {

			throw tempFailure (
				stringFormat (
					"No Dialogue MMS route for message %s",
					outbox.getMessage ().getId ()));

		}

		// initialise any lazy proxies

		dialogueMmsOutbox.outbox.getMessage ().getNumTo ();

		dialogueMmsOutbox.outbox.getMessage ().getText ().getText ();

		dialogueMmsOutbox.outbox.getMessage ().getSubjectText ().getText ();

		for (MediaRec media
				: dialogueMmsOutbox.outbox.getMessage ().getMedias ()) {

			media.getMediaType ().getMimeType ();

			media.getContent ().getData ();

		}

		return dialogueMmsOutbox;

	}

	@Override
	protected
	String sendMessage (
			DialogueMmsOutbox dialogueMmsOutbox)
		throws SendFailureException {

		try {

			log.debug (
				stringFormat (
					"Sending %s",
					dialogueMmsOutbox.outbox.getMessage ().getId ()));

			InputStream inputStream =
				doRequest (dialogueMmsOutbox);

			return doResponse(inputStream);

		} catch (IOException e) {

			throw tempFailure("Got IO error: " + e.getMessage());

		}

	}

	private InputStream doRequest (
			DialogueMmsOutbox dialogueMmsOutbox)
		throws
			SendFailureException,
			IOException {

		MessageRec smsMessage =
			dialogueMmsOutbox.outbox.getMessage ();

		ClientHttpRequest httpRequest =
			new ClientHttpRequest (
				new URL (dialogueMmsOutbox.dialogueMmsRoute.getUrl ()));

		httpRequest.setParameter (
			"X-Mms-Account",
			dialogueMmsOutbox.dialogueMmsRoute.getAccount ());

		httpRequest.setParameter (
			"X-Mms-Username",
			dialogueMmsOutbox.dialogueMmsRoute.getUsername ());

		httpRequest.setParameter (
			"X-Mms-Password",
			dialogueMmsOutbox.dialogueMmsRoute.getPassword ());

		httpRequest.setParameter (
			"X-Mms-Recipient-Addresses",
			smsMessage.getNumTo ());

		httpRequest.setParameter (
			"X-Mms-Originating-Address",
			smsMessage.getNumFrom ());

		httpRequest.setParameter (
			"X-Mms-User-Tag",
			dialogueMmsOutbox.dialogueMmsRoute.getAccount ());

		httpRequest.setParameter (
			"X-Mms-User-Key",
			Integer.toString (
				smsMessage.getId ()));

		if (isNotNull (
				smsMessage.getSubjectText ())) {

			httpRequest.setParameter (
				"X-Mms-Subject",
				smsMessage.getSubjectText ().getText ());

		}

		httpRequest.setParameter (
			"X-Mms-Delivery-Report",
			smsMessage.getRoute ().getDeliveryReports ()
				? "Y"
				: "N");

		if (smsMessage.getRoute ().getDeliveryReports ()) {

			// TODO what?

			httpRequest.setParameter (
				"X-Mms-Reply-Path",
				stringFormat (
					"%s",
					wbsConfig.apiUrl (),
					"/dialogueMMS/route/16/report"));

		}

		StringBuilder smilStringBuilder =
			new StringBuilder ();

		smilStringBuilder.append (
			"<smil><head><layout>");

		// smil+="<root-layout width=\"160\" height=\"120\"/>";

		smilStringBuilder.append (
			"<region id=\"Image\" fit=\"scroll\"/>");

		smilStringBuilder.append (
			"<region id=\"Text\" fit=\"scroll\"/>");

		smilStringBuilder.append (
			"</layout></head><body>");

		List<MediaRec> medias =
			dialogueMmsOutbox.outbox.getMessage ().getMedias ();

		for (
			int index = 0;
			index < medias.size ();
			index += 2
		) {

			MediaRec first =
				medias.get (index);

			MediaRec second = null;

			if (index + 1 < medias.size ()) {

				second =
					medias.get (
						index + 1);
			}

			smilStringBuilder.append (
				"<par dur=\"4s\">");

			if (first.getMediaType ().getId ().intValue () == 1) {

				smilStringBuilder.append (
					stringFormat (
						"<text",
						" region=\"Text\"",
						" src=\"%h\"",
						first.getFilename (),
						"/>"));

			} else {

				smilStringBuilder.append (
					stringFormat (
						"<img",
						" region=\"Image\"",
						" src=\"%h\"",
						first.getFilename (),
						"/>"));

			}

			if (second != null) {

				if (second.getMediaType ().getId ().intValue () == 1) {

					smilStringBuilder.append (
						stringFormat (
							"<text",
							" region=\"Text\"",
							" src=\"%h\"",
							second.getFilename (),
							"/>"));

				} else {

					smilStringBuilder.append (
						stringFormat (
							"<img",
							" region=\"Image\"",
							" src=\"%h\"",
							second.getFilename (),
							"/>"));

				}

			}

			smilStringBuilder.append (
				"</par>");

		}

		smilStringBuilder.append (
			"</body></smil>");

		String smilString =
			smilStringBuilder.toString ();

		log.info (
			stringFormat (
				"SMIL: %s",
				smilString));

		httpRequest.setParameter (
			"filename",
			"ordering.smi",
			new ByteArrayInputStream (smilString.getBytes ()),
			"application/smil; charset=utf-8");

		for (MediaRec media
				: dialogueMmsOutbox.outbox.getMessage ().getMedias ()) {

			String contentType =
				media.getMediaType ().getMimeType ();

			if (media.getEncoding() != null) {

				contentType =
					stringFormat (
						"%s; charset=%s",
						contentType,
						media.getEncoding ());

			}

			httpRequest.setParameter (
				"filename",
				ifNull (
					media.getFilename (),
					"file.jpg"),
				new ByteArrayInputStream (
					media.getContent ().getData ()),
				contentType);

		}

		return httpRequest.post ();

	}

	String doResponse (
			InputStream inputStream)
		throws
			IOException,
			SendFailureException {

		MyHandler handler =
			new MyHandler ();

		try {

			BufferedReader bufferedReader =
				new BufferedReader (
					new InputStreamReader (inputStream));

			StringBuilder stringBuilder =
				new StringBuilder ();

			String line;

			while ((line = bufferedReader.readLine ()) != null) {

				stringBuilder.append (
					line);

			}

			log.info (
				stringFormat (
					"Response: %s",
					stringBuilder.toString ()));

			ByteArrayInputStream byteArrayInputStream =
				new ByteArrayInputStream (
					stringBuilder.toString ().getBytes ("UTF-8"));

			SAXParserFactory saxParserFactory =
				SAXParserFactory.newInstance ();

			SAXParser saxParser =
				saxParserFactory.newSAXParser ();

			saxParser.parse (
				byteArrayInputStream,
				handler);

		} catch (ParserConfigurationException exception) {

			throw new RuntimeException (
				exception);

		} catch (SAXException exception) {

			return null;

		}

		if (handler.statusCode == null) {

			log.error (
				"Invalid XML received: no MessageStatusCode");

			throw tempFailure (
				"Invalid XML received: no MessageStatusCode");

		}
		if (handler.statusText == null) {

			log.error (
				"Invalid XML received: no MessageStatusText");

			throw tempFailure (
				"Invalid XML received: no MessageStatusText");

		}

		if (!handler.statusCode.equals("00")
				|| !handler.messageStatusCode.equals("00")) {

			throw permFailure("Error: " + handler.statusCode + " "
					+ handler.messageStatusCode + " (" + handler.statusText
					+ ")");
		}

		if (handler.messageId == null) {

			log.error("Invalid XML received: no MessageId");

			throw tempFailure("Invalid XML received: no MessageId");

		}

		return handler.messageId;

	}

	enum HandlerState {
		start,
		message,
		results,
		messageId,
		messageStatusCode,
		messageStatusText,
		end;
	};

	static
	class MyHandler
		extends DefaultHandler {

		HandlerState state = HandlerState.start;
		String messageId, statusCode, statusText, messageStatusCode;
		StringBuffer sb;

		@Override
		public
		void startElement (
				String uri,
				String localName,
				String qName,
				Attributes attributes)
			throws SAXException {

			switch (state) {
			case start:
				if (qName.equals("X-Mms-Submission")) {
					state = HandlerState.message;
				}
				return;
			case message:
				if (qName.equals("X-Mms-Error-Code")) {
					if (statusCode != null)
						throw new SAXException("More than one StatusCode tag");
					sb = new StringBuffer();
					state = HandlerState.messageStatusCode;
				} else if (qName.equals("X-Mms-Error-Description")) {
					if (statusText != null)
						throw new SAXException("More than one StatusText tag");
					sb = new StringBuffer();
					state = HandlerState.messageStatusText;
				} else if (qName.equals("X-Mms-Submission")) {
					throw new SAXException("More than one Message tag");
				} else if (qName.equals("X-Mms-Submission-Results")) {
					state = HandlerState.results;
				}
				return;
			case results:
				if (qName.equals("X-Mms-Result")) {
					if (messageId != null)
						throw new SAXException("More than one X-Mms-Result tag");
					messageId = attributes.getValue("X-Mms-ID");
					messageStatusCode = attributes.getValue("X-Mms-Status");
				}
				break;
			case messageId:
			case messageStatusCode:
			case messageStatusText:
				throw new SAXException("Unexpected tag " + qName);
			case end:
				if (qName.equals("X-Mms-Submission"))
					throw new SAXException("More than one Message tag");
			default:
				// just ignore
			}
		}

		@Override
		public
		void endElement (
				String uri,
				String localName,
				String qName)
			throws SAXException {

			switch (state) {

			case message:
				if (qName.equals("X-Mms-Submission"))
					state = HandlerState.end;
				return;

			case results:
				sb = null;
				state = HandlerState.message;
				return;

			case messageStatusCode:
				statusCode = sb.toString();
				sb = null;
				state = HandlerState.message;
				return;

			case messageStatusText:
				statusText = sb.toString();
				sb = null;
				state = HandlerState.message;
				return;

			default:
				// do nothing

			}

		}

		@Override
		public
		void characters (
				char[] ch,
				int start,
				int length)
			throws SAXException {

			switch (state) {
			case messageId:
			case messageStatusCode:
			case messageStatusText:
				sb.append(ch, start, length);
				return;
			default:
				return;
			}

		}

	}

	public static
	class DialogueMmsOutbox {
		OutboxRec outbox;
		DialogueMmsRouteRec dialogueMmsRoute;
	}

}