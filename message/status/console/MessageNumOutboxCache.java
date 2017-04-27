package wbs.sms.message.status.console;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.misc.CachedGetter;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.outbox.console.OutboxConsoleHelper;

@Accessors (fluent = true)
@PrototypeComponent ("messageNumOutboxCache")
public
class MessageNumOutboxCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OutboxConsoleHelper outboxHelper;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	// properties

	@Getter @Setter
	Long sliceId;

	// constructors

	public
	MessageNumOutboxCache () {
		super (5000l);
	}

	// implementation

	@Override
	public
	Long refresh (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"refresh");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			SliceRec slice =
				sliceHelper.findRequired (
					sliceId);

			return outboxHelper.countOlderThan (
				slice,
				transaction.now ().minus (
					Duration.standardSeconds (
						5)));

		}

	}

}
