package wbs.sms.message.status.console;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.DatabaseCachedGetter;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.inbox.console.InboxConsoleHelper;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.cache.CachedGetter;

@Accessors (fluent = true)
@PrototypeComponent ("messageNumInboxCache")
public
class MessageNumInboxCache
	implements CachedGetter <Transaction, Long> {

	// singleton dependencies

	@SingletonDependency
	InboxConsoleHelper inboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	// properties

	@Getter @Setter
	Long sliceId;

	// state

	private
	CachedGetter <Transaction, Long> delegate;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			delegate =
				new DatabaseCachedGetter<> (
					logContext,
					this::refresh,
					Duration.standardSeconds (
						2l));

		}

	}

	// public implementation

	@Override
	public
	Long get (
			@NonNull Transaction context) {

		return delegate.get (
			context);

	}

	// private implementation

	private
	Long refresh (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"refresh");

		) {

			SliceRec slice =
				sliceHelper.findRequired (
					transaction,
					sliceId);

			List <RouteRec> routes =
				routeHelper.findByParent (
					transaction,
					slice);

			Instant olderThan =
				transaction.now ().minus (
					Duration.standardSeconds (
						5));

			return routes.stream ()

				.mapToLong (
					route ->
						inboxHelper.countPendingOlderThan (
							transaction,
							route,
							olderThan))

				.sum ();

		}

	}

}
