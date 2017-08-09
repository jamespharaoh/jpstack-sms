package wbs.sms.tracker.logic;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerObjectHelper;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsSimpleTrackerHandler")
public
class SmsSimpleTrackerHandler
	implements SmsTrackerHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsSimpleTrackerObjectHelper smsSimpleTrackerHelper;

	@SingletonDependency
	SmsTrackerLogic smsTrackerLogic;

	// details

	@Override
	public
	String getTypeCode () {
		return "default";
	}

	// implementation

	@Override
	public
	boolean canSend (
			@NonNull Transaction parentTransaction,
			@NonNull SmsTrackerRec tracker,
			@NonNull NumberRec number,
			@NonNull Optional<Instant> timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canSend");

		) {

			SmsSimpleTrackerRec simpleTracker =
				smsSimpleTrackerHelper.findRequired (
					transaction,
					tracker.getParentId ());

			return smsTrackerLogic.simpleTrackerConsult (
				transaction,
				simpleTracker,
				number,
				timestamp);

		}

	}

}
