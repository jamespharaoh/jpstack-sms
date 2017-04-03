package wbs.sms.message.status.console;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrElseSet;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.status.console.StatusLine;

@SingletonComponent ("messageStatusLine")
public
class MessageStatusLine
	implements StatusLine {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <MessageNumInboxCache> messageNumInboxCacheProvider;

	@PrototypeDependency
	Provider <MessageNumOutboxCache> messageNumOutboxCacheProvider;

	@PrototypeDependency
	Provider <MessageStatusLinePart> messageStatusLinePartProvider;

	// state

	Map <Long, MessageNumInboxCache> numInboxCacheBySliceId =
		new HashMap<> ();

	Map <Long, MessageNumOutboxCache> numOutboxCacheBySliceId =
		new HashMap<> ();

	// details

	@Override
	public
	String getName () {
		return "message";
	}

	// implementation

	@Override
	public
	PagePart get (
			@NonNull TaskLogger parentTaskLogger) {

		return messageStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <String> getUpdateScript (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getUpdateScript");

		Long numInbox =
			countInboxes (
				taskLogger);

		Long numOutbox =
			countOutboxes (
				taskLogger);

		// return

		return futureValue (
			stringFormat (
				"updateMessage (%s, %s);\n",
				integerToDecimalString (
					numInbox),
				integerToDecimalString (
					numOutbox)));

	}

	// private implementation

	private synchronized
	long countInboxes (
			@NonNull TaskLogger parentTaskLogger) {

		Long numInbox = 0l;

		// count inboxes

		for (
			SliceRec slice
				: sliceHelper.findAll ()
		) {

			if (

				privChecker.canRecursive (
					GlobalId.root,
					"inbox_view")

				|| privChecker.canRecursive (
					slice,
					"sms_inbox_view")

			) {

				MessageNumInboxCache numInboxCache =
					mapItemForKeyOrElseSet (
						numInboxCacheBySliceId,
						slice.getId (),
						() -> messageNumInboxCacheProvider.get ()

					.sliceId (
						slice.getId ()

				));

				numInbox +=
					numInboxCache.get ();

			}

		}

		return numInbox;

	}

	private synchronized
	long countOutboxes (
			@NonNull TaskLogger parentTaskLogger) {

		Long numOutbox = 0l;

		// count inboxes

		for (
			SliceRec slice
				: sliceHelper.findAll ()
		) {

			if (

				privChecker.canRecursive (
					GlobalId.root,
					"inbox_view")

				|| privChecker.canRecursive (
					slice,
					"sms_outbox_view")

			) {

				MessageNumOutboxCache numOutboxCache =
					mapItemForKeyOrElseSet (
						numOutboxCacheBySliceId,
						slice.getId (),
						() -> messageNumOutboxCacheProvider.get ()

					.sliceId (
						slice.getId ()

				));

				numOutbox +=
					numOutboxCache.get ();

			}

		}

		return numOutbox;

	}

}
