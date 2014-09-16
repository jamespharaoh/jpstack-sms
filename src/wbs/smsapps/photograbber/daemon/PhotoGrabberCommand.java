package wbs.smsapps.photograbber.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.smsapps.photograbber.model.PhotoGrabberRec;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestObjectHelper;
import wbs.smsapps.photograbber.model.PhotoGrabberRequestRec;

@Log4j
@PrototypeComponent ("photoGrabberCommand")
public
class PhotoGrabberCommand
	implements CommandHandler {

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	MediaLogic mediaUtils;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	PhotoGrabberRequestObjectHelper photoGrabberRequestHelper;

	@Inject
	Random random;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public String[] getCommandTypes () {

		return new String [] {
			"photo_grabber.photo_grabber"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		PhotoGrabberRec photoGrabber =
			(PhotoGrabberRec) (Object)
			objectManager.getParent (
				command);

		String mediaRef =
			receivedMessage.getRest ().trim ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ServiceRec defaultService =
			serviceHelper.findByCode (
				photoGrabber,
				"default");

		receivedMessage.setServiceId (
			defaultService.getId ());

		PhotoGrabberRequestRec photoGrabberRequest =
			new PhotoGrabberRequestRec ()

			.setPhotoGrabber (
				photoGrabber)

			.setNumber (
				message.getNumber ())

			.setThreadId (
				message.getThreadId ())

			.setMediaRef (
				mediaRef)

			.setRequestTime (
				new Date ());

		String mediaUrl;

		try {

			mediaUrl =
				lookupMediaUrl (
					photoGrabber.getUrl (),
					mediaRef);

		} catch (IOException exception) {

			throw new RuntimeException (
				exception);

		}

		if (mediaUrl == null) {

			log.warn("Media url lookup failed (message_id = "
					+ message.getId() + ")");

			photoGrabberRequest
				.setFound (false);

			photoGrabberRequestHelper.insert (
				photoGrabberRequest);

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					photoGrabber,
					"photo_grabber_not_found"),
				message.getThreadId (),
				message.getNumber (),
				serviceHelper.findByCode (photoGrabber, "default"));

			transaction.commit ();

			return Status.processed;

		}

		MediaRec media;
		try {

			media =
				fetchMedia (
					mediaUrl,
					photoGrabber.getJpeg (),
					photoGrabber.getJpegWidth (),
					photoGrabber.getJpegHeight ());

		} catch (IOException e) {
			throw new RuntimeException (e);
		}

		if (media == null) {

			log.warn ("Image download failed (message_id = "
					+ message.getId() + ")");

			photoGrabberRequest.setFound (
				false);

			photoGrabberRequestHelper.insert (
				photoGrabberRequest);

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					photoGrabber,
					"photo_grabber_not_found"),
				message.getThreadId (),
				message.getNumber (),
				serviceHelper.findByCode (photoGrabber, "default"));

			transaction.commit();

			return Status.processed;

		}

		photoGrabberRequest

			.setFound (
				true)

			.setMediaUrl (
				mediaUrl)

			.setMedia (
				media)

			.setCode (
				generateCode (8));

		String text =
			photoGrabber.getBillTemplate ().replaceAll (
				"\\{code\\}",
				photoGrabberRequest.getCode ());

		MessageRec billedMessage =
			messageSender.get ()
				.threadId (message.getThreadId ())
				.number (message.getNumber ())
				.messageString (text)
				.numFrom (photoGrabber.getBillNumber ())
				.route (photoGrabber.getBillRoute ())
				.service (serviceHelper.findByCode (photoGrabber, "default"))
				.deliveryTypeCode ("photo_grabber")
				.send ();

		photoGrabberRequest.setBilledMessage (billedMessage);

		photoGrabberRequestHelper.insert (
			photoGrabberRequest);

		transaction.commit ();

		return Status.processed;

	}

	private
	MediaRec fetchMedia (
			String url,
			boolean jpeg,
			int jpegWidth,
			int jpegHeight)
		throws IOException {

		byte[] data =
			fetchUrlData (url);

		return mediaUtils.createMediaFromImage (
			data,
			"image/jpeg",
			null);

	}

	enum HandlerState {

		start,
		inResponse,
		inFound,
		inUrl,
		end;

	};

	static
	class MyHandler
		extends DefaultHandler {

		HandlerState state =
			HandlerState.start;

		String found;
		String url;

		StringBuilder stringBuilder;

		@Override
		public
		void startElement (
				String uri,
				String localName,
				String qualifiedName,
				Attributes attributes)
			throws SAXException {

			switch (state) {

			case start:

				if (
					equal (
						qualifiedName,
						"photo-grabber-response")
				) {

					state =
						HandlerState.inResponse;

					return;

				} else {

					throw new SAXException (
						"First tag must be photograbber-response");

				}

			case inResponse:

				if (
					equal (
						qualifiedName,
						"found")
				) {

					if (found != null) {

						throw new SAXException (
							"More than one found tag");

					}

					stringBuilder =
						new StringBuilder ();

					state =
						HandlerState.inFound;

					return;

				} else if (qualifiedName.equals ("url")) {

					if (url != null)
						throw new SAXException ("More than one URL tag");

					stringBuilder =
						new StringBuilder ();

					state = HandlerState.inUrl;

					return;

				} else {

					throw new SAXException (
						stringFormat (
							"Invalid tag <%s> inside <photograbber-response>",
							qualifiedName));

				}

			default:

				throw new SAXException (
					"Unexpected tag " + qualifiedName);

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

			case inResponse:

				state =
					HandlerState.end;

				return;

			case inFound:

				found =
					stringBuilder.toString ();

				stringBuilder =
					null;

				state =
					HandlerState.inResponse;

				return;

			case inUrl:

				url =
					stringBuilder.toString ();

				stringBuilder =
					null;

				state =
					HandlerState.inResponse;

				return;

			default:

				throw new RuntimeException (
					"Logic error");

			}

		}

		@Override
		public
		void characters (
				char[] character,
				int start, int length)
			throws SAXException {

			switch (state) {

			case inFound:
			case inUrl:

				stringBuilder.append (
					character,
					start,
					length);

				return;

			default:

				String string =
					new String (
							character,
							start,
							length)
						.trim ();

				if (string.length () != 0) {

					throw new SAXException (
						"Extra characters found");

				}

			}

		}

	}

	public static
	String lookupMediaUrl (
			String baseUrl,
			String ref)
		throws IOException {

		// open the url

		URL url;

		try {

			url =
				new URL (
					stringFormat (
						"%s",
						baseUrl,
						"?ref=%u",
						ref));

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

		URLConnection urlConnection =
			url.openConnection ();

		InputStream inputStream =
			urlConnection.getInputStream ();

		// parse the xml

		MyHandler handler =
			new MyHandler ();

		try {

			SAXParserFactory factory =
				SAXParserFactory.newInstance ();

			SAXParser saxParser =
				factory.newSAXParser ();

			saxParser.parse (
				inputStream,
				handler);

		} catch (ParserConfigurationException exception) {

			throw new RuntimeException (
				exception);

		} catch (SAXException exception) {

			log.error (
				stringFormat (
					"Error parsing response from %s",
					baseUrl),
				exception);

			return null;

		}

		// check the result

		if (handler.found == null) {

			log.error (
				stringFormat (
					"Response from %s contained no <found> element",
					baseUrl));

			return null;

		}

		if (
			notEqual (
				handler.found.toLowerCase (),
				"true")
		) {

			log.warn("Got not found from " + baseUrl + ", ref was " + ref);

			return null;

		}

		if (handler.url == null) {

			log.error("Response from " + baseUrl
					+ " contained found=true but no url.");

			return null;

		}

		return handler.url;

	}

	byte[] fetchUrlData (
			String url)
		throws IOException {

		try {

			// open the url

			URLConnection urlConnection =
				new URL (url).openConnection ();

			InputStream inputStream =
				urlConnection.getInputStream ();

			// load the data

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream ();

			byte[] bytes =
				new byte[8192];

			int numRead;

			while ((numRead = inputStream.read (bytes)) > 0) {

				byteArrayOutputStream.write (
					bytes,
					0,
					numRead);

			}

			// return

			return byteArrayOutputStream.toByteArray ();

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	String generateCode (
			int length) {

		char[] chars =
			new char [length];

		for (
			int i = 0;
			i < length;
			i ++
		) {

			chars [i] =
				codeChars.charAt (
					random.nextInt (
						codeChars.length ()));

		}

		return new String (
			chars);

	}

	// data

	static final
	String codeChars =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ";

}