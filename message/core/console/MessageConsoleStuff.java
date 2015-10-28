package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@SingletonComponent ("messageConsoleStuff")
public
class MessageConsoleStuff {

	// dpendencies

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	// implementation

	@Deprecated
	public static
	String classForMessage (
			MessageRec message) {

		if (message.getDirection () == MessageDirection.in) {

			return "message-in";

		} else if (message.getCharge () > 0) {

			return "message-out-charge";

		} else {

			return "message-out";

		}

	}

	@Deprecated
	public static
	String classForMessageDirection (
			MessageDirection direction) {

		switch (direction) {

		case in:
			return "message-in";

		case out:
			return "message-out";

		}

		throw new IllegalArgumentException ();

	}

	@Deprecated
	public static
	String classForMessageStatus (
			MessageStatus messageStatus) {

		if (messageStatus.isGoodType ())
			return "message-status-succeeded";

		if (messageStatus.isBadType ())
			return "message-status-failed";

		return "message-status-unknown";

	}

	@Deprecated
	public static
	char charForMessageStatus (
			MessageStatus messageStatus) {

		switch (messageStatus) {

		case pending:
			return ' ';

		case processed:
			return 'p';

		case cancelled:
			return 'c';

		case failed:
			return 'f';

		case sent:
			return '-';

		case delivered:
			return 'd';

		case undelivered:
		case manuallyUndelivered:
			return 'u';

		case notProcessed:
			return 'n';

		case ignored:
			return 'i';

		case manuallyProcessed:
			return 'm';

		case submitted:
			return '+';

		case reportTimedOut:
			return '?';

		case held:
			return 'h';

		case blacklisted:
			return 'b';

		}

		throw new IllegalArgumentException (
			"Unknown message status");

	}

	@Deprecated
	public static
	String textForMessageStatus (
			MessageStatus messageStatus) {

		return messageStatus.getDescription ();

	}

	@Deprecated
	public static
	String tdForMessageStatus (
			MessageStatus messageStatus) {

		return stringFormat (
			"<td class=\"%h\">%h</td>",
			classForMessageStatus (
				messageStatus),
			textForMessageStatus (
				messageStatus));

	}

	@Deprecated
	public static
	String textForMessageDirection (
			MessageDirection direction) {

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
