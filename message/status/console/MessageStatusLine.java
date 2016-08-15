package wbs.sms.message.status.console;

import static wbs.framework.utils.etc.ConcurrentUtils.futureValue;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.status.console.StatusLine;

@SingletonComponent ("messageStatusLine")
public
class MessageStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MessageNumInboxCache numInboxCache;

	@Inject
	MessageNumOutboxCache numOutboxCache;

	@Inject
	UserPrivChecker privChecker;

	// prototype dependencies

	@Inject
	Provider<MessageStatusLinePart> messageStatusLinePart;

	// details

	@Override
	public
	String getName () {
		return "message";
	}

	// implementation

	@Override
	public
	PagePart get () {

		return messageStatusLinePart.get ();

	}

	@Override
	public
	Future<String> getUpdateScript () {

		int numInbox = 0;
		int numOutbox = 0;

		// count inboxes (if visible)

		if (
			privChecker.canRecursive (
				GlobalId.root,
				"inbox_view")
		) {

			numInbox =
				numInboxCache.get ();

		}

		// count outboxes (if visible)

		if (
			privChecker.canRecursive (
				GlobalId.root,
				"outbox_view")
		) {

			numOutbox =
				numOutboxCache.get ();

		}

		// return

		return futureValue (
			stringFormat (
				"updateMessage (%d, %d);\n",
				numInbox,
				numOutbox));

	}

}
