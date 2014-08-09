package wbs.sms.message.status.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.status.console.StatusLine;

@SingletonComponent ("messageStatusLine")
public
class MessageStatusLine
	implements StatusLine {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MessageNumInboxCache numInboxCache;

	@Inject
	MessageNumOutboxCache numOutboxCache;

	@Inject
	MessageNumNotProcessedCache numNotProcessedCache;

	@Inject
	PrivChecker privChecker;

	@Inject Provider<MessageStatusLinePart> messageStatusLinePart;

	@Override
	public
	String getName () {
		return "message";
	}

	@Override
	public
	PagePart get () {

		return messageStatusLinePart.get ();

	}

	@Override
	public
	String getUpdateScript () {

		int numInbox = 0;
		int numOutbox = 0;
		int numNotProcessed = 0;

		// count inboxes (if visible)

		if (privChecker.can (
				GlobalId.root,
				"inbox_view")) {

			numInbox =
				numInboxCache.get ();

		}

		// count outboxes (if visible)

		if (privChecker.can (
				GlobalId.root,
				"outbox_view")) {

			numOutbox =
				numOutboxCache.get ();

		}

		// count not processed (if visible)

		if (privChecker.can (
				GlobalId.root,
				"message_notprocessed_view")) {

			numNotProcessed =
				numNotProcessedCache.get ();

		}

		// return

		return stringFormat (
			"updateMessage (%d, %d, %d);\n",
			numInbox,
			numOutbox,
			numNotProcessed);

	}

}
