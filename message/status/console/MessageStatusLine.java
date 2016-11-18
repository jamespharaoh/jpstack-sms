package wbs.sms.message.status.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.concurrent.Future;

import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;

import wbs.platform.status.console.StatusLine;

@SingletonComponent ("messageStatusLine")
public
class MessageStatusLine
	implements StatusLine {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	MessageNumInboxCache numInboxCache;

	@SingletonDependency
	MessageNumOutboxCache numOutboxCache;

	@SingletonDependency
	UserPrivChecker privChecker;

	// prototype dependencies

	@PrototypeDependency
	Provider <MessageStatusLinePart> messageStatusLinePartProvider;

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

		return messageStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <String> getUpdateScript () {

		Long numInbox = 0l;
		Long numOutbox = 0l;

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
				"updateMessage (%s, %s);\n",
				integerToDecimalString (
					numInbox),
				integerToDecimalString (
					numOutbox)));

	}

}
