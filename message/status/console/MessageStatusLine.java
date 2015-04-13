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

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MessageNumInboxCache numInboxCache;

	@Inject
	MessageNumOutboxCache numOutboxCache;

	@Inject
	PrivChecker privChecker;

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
	String getUpdateScript () {

		int numInbox = 0;
		int numOutbox = 0;

		// count inboxes (if visible)

		if (
			privChecker.can (
				GlobalId.root,
				"inbox_view")
		) {

			numInbox =
				numInboxCache.get ();

		}

		// count outboxes (if visible)

		if (
			privChecker.can (
				GlobalId.root,
				"outbox_view")
		) {

			numOutbox =
				numOutboxCache.get ();

		}

		// return

		return stringFormat (
			"updateMessage (%d, %d);\n",
			numInbox,
			numOutbox);

	}

}
