package wbs.sms.route.http.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.logic.NetworkPrefixCache;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.http.model.HttpRouteObjectHelper;
import wbs.sms.route.http.model.HttpRouteRec;

/**
 * Daemon service to process outbox items in http routes.
 *
 * TODO this does not belong here
 */
@Log4j
@SingletonComponent ("httpSender")
public
class HttpSender
	extends AbstractSmsSender1<HttpSender.HttpOutbox> {

	// dependencies

	@Inject
	HttpRouteObjectHelper httpRouteHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NetworkPrefixCache networkPrefixCache;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	@Inject
	WbsConfig wbsConfig;

	// details

	@Override
	protected
	String getThreadName () {
		return "HttpSndr";
	}

	@Override
	protected
	String getSenderCode () {
		return "http";
	}

	// implementation

	@Override
	protected
	HttpOutbox getMessage (
			OutboxRec outbox)
		throws SendFailureException {

		HttpRouteRec httpRoute =
			selectRoute (outbox);

		if (httpRoute == null) {

			log.error (
				stringFormat (
					"No network information for message %s",
					outbox.getMessage ().getId ()));

			throw permFailure (
				"No network information or no route for network");

		}

		WapPushMessageRec wapPushMessage = null;

		if (equal (
				outbox.getMessage ().getMessageType ().getCode (),
				"wap_push")) {

			wapPushMessage =
				wapPushMessageHelper.find (
					outbox.getId ());

			if (wapPushMessage == null) {

				throw tempFailure (
					"Wap push message not found for message "
						+ outbox.getId ());

			}

			wapPushMessage
				.getUrlText ()
				.getText ();

			wapPushMessage
				.getTextText ()
				.getText ();

		}

		return new HttpOutbox (
			outbox,
			httpRoute,
			wapPushMessage);

	}

	@Override
	protected
	String sendMessage (
			HttpOutbox httpOutbox)
		throws SendFailureException {

		log.info (
			stringFormat (
				"Sending message %s",
				httpOutbox.messageId));

		try {

			// do the request

			HttpURLConnection urlConnection =
				httpOutbox.httpRoute.getPost ()
					? openPost (httpOutbox)
					: openGet (httpOutbox);

			// read the response

			String response =
				readResponse (
					urlConnection);

			// check the response

			String otherId =
				checkResponse (
					httpOutbox,
					response);

			// and return

			return otherId;

		} catch (IOException exception) {

			throw tempFailure (
				stringFormat (
					"IO error %s",
					 exception.getMessage ()));

		}

	}

	HttpRouteRec selectRoute (
			OutboxRec outbox) {

		HttpRouteRec httpRoute;

		// if a network is specified try that

		if (outbox.getMessage ().getNetwork () != null) {

			httpRoute =
				httpRouteHelper.find (
					outbox.getMessage ().getRoute (),
					outbox.getMessage ().getNetwork ());

			if (httpRoute != null)
				return httpRoute;

		}

		// then look for a non-specific route

		NetworkRec defaultNetwork =
			networkHelper.find (0);

		httpRoute =
			httpRouteHelper.find (
				outbox.getMessage ().getRoute (),
				defaultNetwork);

		if (httpRoute != null)
			return httpRoute;

		// finally try looking up the number in the network prefixes list and
		// use that httpRoute

		NetworkRec network =
			networkPrefixCache.lookupNetwork (
				outbox.getMessage ().getNumTo ());

		if (network != null) {

			httpRoute =
				httpRouteHelper.find (
					outbox.getRoute (),
					network);

			if (httpRoute != null)
				return httpRoute;

		}

		// not found

		return null;

	}

	/**
	 * Reads the response from the connection into into a string (assumes utf-8
	 * encoding).
	 */
	String readResponse (
			HttpURLConnection urlConn)
		throws
			IOException,
			UnsupportedEncodingException {

		Reader reader =
			new InputStreamReader (
				urlConn.getInputStream (),
				"utf-8");

		StringBuffer responseBuffer =
			new StringBuffer ();

		int numread;

		char buffer[] =
			new char [1024];

		while ((numread = reader.read (buffer, 0, 1024)) > 0)
			responseBuffer.append (buffer, 0, numread);

		return responseBuffer.toString ();

	}

	String checkResponse (
			HttpOutbox httpOutbox,
			String response)
		throws SendFailureException {

		// check for success

		Pattern successPattern =
			Pattern.compile (
				httpOutbox.httpRoute.getSuccessRegex ());

		Matcher successMatcher =
			successPattern.matcher (
				response);

		if (successMatcher.find ()) {

			String otherId =
				successMatcher.groupCount () >= 1
					? successMatcher.group (1)
					: null;

			return otherId;

		}

		// check for known failures...

		if (httpOutbox.httpRoute.getTempFailureRegex () != null) {

			Pattern tempFailurePattern =
				Pattern.compile (
					httpOutbox.httpRoute.getTempFailureRegex ());

			Matcher tempFailureMatcher =
				tempFailurePattern.matcher (response);

			if (tempFailureMatcher.find ()) {

				String fullError =
					stringFormat (
						"Server returned temporary failure: %s",
						response);

				log.warn (
					fullError);

				throw tempFailure (
					fullError);

			}

		}

		if (httpOutbox.httpRoute.getPermFailureRegex () != null) {

			Pattern permFailurePattern =
				Pattern.compile (
					httpOutbox.httpRoute.getPermFailureRegex ());

			Matcher permFailureMatcher =
				permFailurePattern.matcher (
					response);

			if (permFailureMatcher.find ()) {

				String fullError =
					stringFormat (
						"Server returned permanent failure: %s",
						response);

				log.error (
					fullError);

				throw permFailure (
					fullError);

			}

		}

		if (httpOutbox.httpRoute.getDailyFailureRegex () != null) {

			Pattern dailyFailurePattern =
				Pattern.compile (
					httpOutbox.httpRoute.getDailyFailureRegex ());

			Matcher dailyFailureMatcher =
				dailyFailurePattern.matcher (
					response);

			if (dailyFailureMatcher.find ()) {

				String fullError =
					stringFormat (
						"Server returned daily failure: %s",
						response);

				log.warn (
					fullError);

				throw dailyFailure (
					fullError);

			}
		}

		if (httpOutbox.httpRoute.getCreditFailureRegex () != null) {

			Pattern creditFailurePattern =
				Pattern.compile (
					httpOutbox.httpRoute.getCreditFailureRegex ());

			Matcher creditFailureMatcher =
				creditFailurePattern.matcher (
					response);

			if (creditFailureMatcher.find ()) {

				String fullError =
					stringFormat (
						"Server returned credit failure: %s",
						response);


				log.warn (
					fullError);

				throw dailyFailure (
					fullError);

			}

		}

		// unknown response, temporary failure

		String fullError =
			stringFormat (
				"Server returned unknown error: %s",
				response);

		log.warn (
			fullError);

		throw tempFailure (
			fullError);

	}

	String getParams (
			HttpOutbox httpOutbox)
		throws UnsupportedEncodingException {

		String paramsTemplate =
			httpOutbox.httpRoute.getParams ();

		String paramEncoding =
			httpOutbox.httpRoute.getParamEncoding ();

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher paramsMatcher =
			paramsPattern.matcher (
				paramsTemplate);

		int index = 0;

		while (paramsMatcher.find ()) {

			String param =
				paramsTemplate.substring (
					index,
					paramsMatcher.start ());

			String paramName =
				paramsMatcher.group (1);

			if (paramName.equals ("title")
					|| paramName.equals ("url")
					|| paramName.equals ("media")
					|| paramName.equals ("terminal")) {

				if (httpOutbox.wapPushMessage == null) {

					index =
						paramsMatcher.end ();

					continue;

				}

			}

			// TODO should not be here

			if (httpOutbox.wapPushMessage != null) {

				if (paramName.equals ("message")
						|| paramName.equals ("hexmessage")) {

					index =
						paramsMatcher.end ();

					continue;

				}

			}

			stringBuilder.append (
				param);

			if (paramName.equals ("id")) {

				stringBuilder.append (
					URLEncoder.encode (
						Integer.toString (
							httpOutbox.messageId),
						paramEncoding));

			} else if (paramName.equals ("message")) {

				stringBuilder.append (
					URLEncoder.encode (
						httpOutbox.message.getText ().getText (),
						paramEncoding));

			} else if (paramName.equals ("hexmessage")) {

				stringBuilder.append (
					toHex (
						httpOutbox.message.getText ().getText (),
						paramEncoding));

			} else if (paramName.equals ("numfrom")) {

				stringBuilder.append (
					URLEncoder.encode (
						httpOutbox.message.getNumFrom (),
						paramEncoding));

			} else if (paramName.equals ("numto")) {

				stringBuilder.append (
					URLEncoder.encode (
						httpOutbox.message.getNumTo (),
						paramEncoding));

			} else if (paramName.equals ("title")) {

				if (httpOutbox.wapPushMessage != null) {

					stringBuilder.append (
						URLEncoder.encode (
							httpOutbox.wapPushMessage.getTextText ().getText (),
							paramEncoding));

				}

			} else if (paramName.equals ("url")) {

				if (httpOutbox.wapPushMessage != null) {

					String url =
						httpOutbox.wapPushMessage.getUrlText ().getText ();

					String prefix =
						"http://";

					if (url.startsWith (prefix)) {

						url =
							url.substring (
								prefix.length (),
								url.length ());

					}

					stringBuilder.append (
						toHex (
							url,
							paramEncoding));

				}

			} else if (paramName.equals ("media")) {

				if (httpOutbox.wapPushMessage != null) {

					stringBuilder.append (
						URLEncoder.encode (
							"PI",
							paramEncoding));

				}

			} else if (paramName.equals ("terminal")) {

				if (httpOutbox.wapPushMessage != null) {

					stringBuilder.append (
						URLEncoder.encode (
							"WAP",
							paramEncoding));

				}

			} else if (paramName.equals ("comshen_ton_hack")) {

				// should not be here

				String senderTon = "5";

				if (httpOutbox.message.getNumFrom ().startsWith ("0"))
					senderTon = "1";

				if (httpOutbox.message.getNumFrom ().startsWith ("6"))
					senderTon = "3";

				if (httpOutbox.message.getNumFrom ().startsWith ("8"))
					senderTon = "3";

				stringBuilder.append (
					senderTon);

			}

			index =
				paramsMatcher.end ();

		}

		stringBuilder.append (
			paramsTemplate.substring (index));

		log.debug (
			stringFormat (
				"STRING IS %s",
				stringBuilder.toString ()));

		return stringBuilder.toString ();

	}

	StringBuilder toHex (
			String url,
			String paramEncoding) {

		char[] chars =
			url.toCharArray ();

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (
			int index = 0;
			index < chars.length;
			index ++
		) {

			stringBuilder.append (
				Integer.toHexString (
					(int) chars [index]));

		}

		return stringBuilder;

	}

	public
	HttpURLConnection openPost (
			HttpOutbox httpOutbox)
		throws IOException {

		String params =
			getParams (httpOutbox);

		// create and open http connection

		URL urlObject =
			new URL (
				httpOutbox.httpRoute.getUrl ());

		HttpURLConnection urlConnection =
			(HttpURLConnection)
			urlObject.openConnection ();

		urlConnection.setDoInput (
			true);

		urlConnection.setDoOutput (
			true);

		urlConnection.setAllowUserInteraction (
			false);

		urlConnection.setRequestMethod (
			"POST");

		urlConnection.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		urlConnection.setRequestProperty (
			"Content-Type",
			stringFormat (
				"application/x-www-form-urlencoded; ",
				"charset=\"%s\"",
				httpOutbox.httpRoute.getParamEncoding ()));

		urlConnection.setRequestProperty (
			"Content-Length",
			Integer.toString (
				params.length ()));

		log.debug (
			stringFormat (
				"Sending: %s",
				params));

		// send request

		Writer writer =
			new OutputStreamWriter (
				urlConnection.getOutputStream (),
				"iso-8859-1");

		writer.write (params);

		writer.flush ();

		return urlConnection;

	}

	public
	HttpURLConnection openGet (
			HttpOutbox httpOutbox)
		throws IOException {

		// create and open http connection

		URL urlObj =
			new URL (
				stringFormat (
					"%s",
					httpOutbox.httpRoute.getUrl (),
					"?%s",
					getParams (httpOutbox)));

		HttpURLConnection urlConnection =
			(HttpURLConnection)
			urlObj.openConnection ();

		urlConnection.setDoInput (
			true);

		urlConnection.setAllowUserInteraction (
			false);

		urlConnection.setRequestMethod (
			"GET");

		urlConnection.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		// return

		return urlConnection;

	}

	public static
	class HttpOutbox {

		int messageId;
		OutboxRec outbox;
		MessageRec message;
		NetworkRec network;
		HttpRouteRec httpRoute;
		WapPushMessageRec wapPushMessage;

		HttpOutbox (
				OutboxRec outbox,
				HttpRouteRec httpRoute,
				WapPushMessageRec wapPushMessage) {

			this.outbox =
				outbox;

			this.messageId =
				outbox.getId ();

			this.message =
				outbox.getMessage ();

			this.network =
				message.getNetwork ();

			this.httpRoute =
				httpRoute;

			this.wapPushMessage =
				wapPushMessage;

			message.getText ().getText ();

		}

	}

	static
	Pattern paramsPattern =
		Pattern.compile (
			"\\{(hexmessage|message|numfrom|numto|id|url|title|media|terminal"
				+ "|comshen_ton_hack)\\}");

}
