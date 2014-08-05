package wbs.sms.message.ticker.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
import wbs.platform.media.console.MediaConsoleHelper;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.priv.console.PrivChecker;
import wbs.sms.message.core.console.MessageConsoleStuff;
import wbs.sms.message.core.model.MessageDirection;

@PrototypeComponent ("messageTickerUpdateResponder")
public
class MessageTickerUpdateResponder
	extends ConsoleResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaConsoleHelper mediaHelper;

	@Inject
	MessageTickerManager messageTickerManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	TimeFormatter timeFormatter;

	List<String> commands =
		new ArrayList<String> ();

	int newGeneration = 0;

	@Override
	public
	void prepare () {

		Collection<MessageTickerMessage> messageTickerMessages =
			messageTickerManager.getMessages ();

		int generation =
			Integer.parseInt (requestContext.parameter ("gen"));

		for (MessageTickerMessage messageTickerMessage
				: messageTickerMessages) {

			if (

				! privChecker.can (
					messageTickerMessage.routeGlobalId (),
					"messages")

				&& ! privChecker.can (
					messageTickerMessage.serviceParentGlobalId (),
					"messages")

				&& ! privChecker.can (
					messageTickerMessage.affiliateParentGlobalId (),
					"messages")

			) {
				continue;
			}

			if (messageTickerMessage.messageGeneration ()
					> generation) {

				commands.add (
					doMessage (messageTickerMessage));

			}

			if (messageTickerMessage.messageGeneration ()
					> newGeneration) {

				newGeneration =
					messageTickerMessage.messageGeneration ();

			}

		}

		for (MessageTickerMessage messageTickerMessage
				: messageTickerMessages) {

			if (

				! privChecker.can (
					messageTickerMessage.routeGlobalId (),
					"messages")

				&& ! privChecker.can (
					messageTickerMessage.serviceParentGlobalId (),
					"messages")

				&& ! privChecker.can (
					messageTickerMessage.affiliateParentGlobalId (),
					"messages")

			) {
				continue;
			}

			if (messageTickerMessage.statusGeneration ()
					> generation) {

				commands.add (
					doStatus (messageTickerMessage));

			}

			if (messageTickerMessage.statusGeneration ()
					> newGeneration) {

				newGeneration =
					messageTickerMessage.statusGeneration ();

			}

		}

	}

	@Override
	public
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/javascript; charset=utf-8");

	}

	@Override
	public
	void goContent () {

		PrintWriter writer =
			requestContext.writer ();

		writer.print (
			stringFormat (
				"messageTicker.generation = %d;\n",
				newGeneration));

		for (String command : commands) {

			writer.print (
				stringFormat (
					"%s\n",
					command));

		}

	}

	private
	String doMessage (
			MessageTickerMessage messageTickerMessage) {

		String rowClass;

		if (messageTickerMessage.direction () == MessageDirection.in) {

			rowClass =
				"message-in";

		} else if (messageTickerMessage.charge () > 0) {

			rowClass =
				"message-out-charge";

		} else {

			rowClass =
				"message-out";

		}

		String number =
			messageTickerMessage.direction () == MessageDirection.in
				? messageTickerMessage.numFrom ()
				: messageTickerMessage.numTo ();

		String color =
			Html.genHtmlColor (
				number);

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			stringFormat (

				"messageTicker.addRow ('%j'",
				rowClass,

				", '%j'",
				color,

				", '%j'",
				requestContext.resolveApplicationUrl (
					stringFormat (
						"/message",
						"/%u",
						messageTickerMessage.messageId (),
						"/message.summary")),

				", [ '%j'",
				timeFormatter.instantToTimeString (
					timeFormatter.defaultTimezone (),
					messageTickerMessage.createdTime ()),

				", '%j'",
				messageTickerMessage.numFrom (),

				", '%j',",
				messageTickerMessage.numTo (),

				" '%j'",
				messageTickerMessage.text (),

				" ], [ "));

		boolean comma = false;

		for (Integer mediaId
				: messageTickerMessage.mediaIds ()) {

			MediaRec media =
				mediaHelper.find (
					mediaId);

			if (comma) {
				stringBuilder.append (", ");
			} else {
				comma = true;
			}

			stringBuilder.append (
				stringFormat (
					"'%j'",
					mediaConsoleLogic.mediaThumb32OrText (media)));

		}

		stringBuilder.append (
			stringFormat (

				" ], %s",
				messageTickerMessage.messageId (),

				", '%j'",
				MessageConsoleStuff.classForMessageStatus (
					messageTickerMessage.status ()),

				", '%j');",
				Character.toString (
					MessageConsoleStuff.charForMessageStatus (
						messageTickerMessage.status ()))));

		return stringBuilder.toString ();

	}

	private
	String doStatus (
			MessageTickerMessage messageTickerMessage) {

		return stringFormat (
			"messageTicker.updateStatus (%s, '%j', '%j');",

			messageTickerMessage.messageId (),

			MessageConsoleStuff.classForMessageStatus (
				messageTickerMessage.status ()),

			Character.toString (
				MessageConsoleStuff.charForMessageStatus (
					messageTickerMessage.status ())));

	}

}
