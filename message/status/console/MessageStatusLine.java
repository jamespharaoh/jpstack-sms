package wbs.sms.message.status.console;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrElseSet;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Provider;

import com.google.gson.JsonObject;

import lombok.NonNull;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

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
	String typeName () {
		return "sms-messages";
	}

	// implementation

	@Override
	public
	PagePart createPagePart (
			@NonNull Transaction parentTransaction) {

		return messageStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <JsonObject> getUpdateData (
			@NonNull Transaction parentTransaction,
			@NonNull UserPrivChecker privChecker) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getUpdateData");

		) {

			JsonObject jsonObject =
				new JsonObject ();

			jsonObject.addProperty (
				"inbox",
				countInboxes (
					transaction,
					privChecker));

			jsonObject.addProperty (
				"outbox",
				countOutboxes (
					transaction,
					privChecker));

			return futureValue (
				jsonObject);

		}

	}

	// private implementation

	private synchronized
	long countInboxes (
			@NonNull Transaction parentTransaction,
			@NonNull UserPrivChecker privChecker) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"countInboxes");

		) {

			Long numInbox = 0l;

			// count inboxes

			for (
				SliceRec slice
					: sliceHelper.findAll (
						transaction)
			) {

				if (

					privChecker.canRecursive (
						transaction,
						GlobalId.root,
						"inbox_view")

					|| privChecker.canRecursive (
						transaction,
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
						numInboxCache.get (
							transaction);

				}

			}

			return numInbox;

		}

	}

	private synchronized
	long countOutboxes (
			@NonNull Transaction parentTransaction,
			@NonNull UserPrivChecker privChecker) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"countOutboxes");

		) {

			Long numOutbox = 0l;

			// count inboxes

			for (
				SliceRec slice
					: sliceHelper.findAll (
						transaction)
			) {

				if (

					privChecker.canRecursive (
						transaction,
						GlobalId.root,
						"inbox_view")

					|| privChecker.canRecursive (
						transaction,
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
						numOutboxCache.get (
							transaction);

				}

			}

			return numOutbox;

		}

	}

}
