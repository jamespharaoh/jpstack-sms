package wbs.sms.message.ticker.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlUtils.htmlColourFromObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleHelper;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("messageTickerUpdateResponder")
public
class MessageTickerUpdateResponder
	extends ConsoleResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaConsoleHelper mediaHelper;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageTickerManager messageTickerManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	List<String> commands =
		new ArrayList<String> ();

	long newGeneration = 0;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		Collection<MessageTickerMessage> messageTickerMessages =
			messageTickerManager.getMessages ();

		long generation =
			Long.parseLong (
				requestContext.parameterRequired (
					"gen"));

		for (
			MessageTickerMessage messageTickerMessage
				: messageTickerMessages
		) {

			if (

				! privChecker.canRecursive (
					messageTickerMessage.routeGlobalId (),
					"messages")

				&& ! privChecker.canRecursive (
					messageTickerMessage.serviceParentGlobalId (),
					"messages")

				&& ! privChecker.canRecursive (
					messageTickerMessage.affiliateParentGlobalId (),
					"messages")

			) {
				continue;
			}

			if (messageTickerMessage.messageGeneration ()
					> generation) {

				commands.add (
					doMessage (
						taskLogger,
						messageTickerMessage));

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

				! privChecker.canRecursive (
					messageTickerMessage.routeGlobalId (),
					"messages")

				&& ! privChecker.canRecursive (
					messageTickerMessage.serviceParentGlobalId (),
					"messages")

				&& ! privChecker.canRecursive (
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
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/javascript; charset=utf-8");

	}

	@Override
	public
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		FormatWriter formatWriter =
			requestContext.formatWriter ();

		formatWriter.writeLineFormat (
			"messageTicker.generation = %s;",
			integerToDecimalString (
				newGeneration));

		commands.forEach (
			command ->
				formatWriter.writeLineFormat (
					"%s",
					command));

	}

	private
	String doMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageTickerMessage messageTickerMessage) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doMessage");

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
			htmlColourFromObject (
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
				requestContext.resolveApplicationUrlFormat (
					"/message",
					"/%u",
					integerToDecimalString (
						messageTickerMessage.messageId ()),
					"/message.summary"),

				", [ '%j'",
				userConsoleLogic.timeString (
					messageTickerMessage.createdTime ()),

				", '%j'",
				messageTickerMessage.numFrom (),

				", '%j',",
				messageTickerMessage.numTo (),

				" '%j'",
				messageTickerMessage.text (),

				" ], [ "));

		boolean comma = false;

		for (
			Long mediaId
				: messageTickerMessage.mediaIds ()
		) {

			MediaRec media =
				mediaHelper.findRequired (
					mediaId);

			if (comma) {
				stringBuilder.append (", ");
			} else {
				comma = true;
			}

			stringBuilder.append (
				stringFormat (
					"'%j'",
					formatWriterConsumerToString (
						formatWriter ->
							mediaConsoleLogic.writeMediaThumb32OrText (
								taskLogger,
								formatWriter,
								media))));

		}

		stringBuilder.append (
			stringFormat (
				" ], %s",
				integerToDecimalString (
					messageTickerMessage.messageId ())));

		stringBuilder.append (
			stringFormat (
				", '%j'",
				messageConsoleLogic.classForMessageStatus (
					messageTickerMessage.status ())));

		stringBuilder.append (
			stringFormat (
				", '%j');",
				Character.toString (
					messageConsoleLogic.charForMessageStatus (
						messageTickerMessage.status ()))));

		return stringBuilder.toString ();

	}

	private
	String doStatus (
			@NonNull MessageTickerMessage messageTickerMessage) {

		return stringFormat (
			"messageTicker.updateStatus (%s, '%j', '%j');",

			integerToDecimalString (
				messageTickerMessage.messageId ()),

			messageConsoleLogic.classForMessageStatus (
				messageTickerMessage.status ()),

			Character.toString (
				messageConsoleLogic.charForMessageStatus (
					messageTickerMessage.status ())));

	}

}
