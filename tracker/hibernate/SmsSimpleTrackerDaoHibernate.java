package wbs.sms.tracker.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.hibernate.MessageDirectionType;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerDao;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
class SmsSimpleTrackerDaoHibernate
	extends HibernateDaoLegacy
	implements SmsSimpleTrackerDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <MessageRec> findMessages (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMessages");

		) {

			return findMany (
				MessageRec.class,

				createQuery (
					transaction,
					"SELECT message " +
					"FROM MessageRec message, " +
						"SmsSimpleTrackerRouteRec smsSimpleTrackerRoute " +
					"WHERE smsSimpleTrackerRoute.smsSimpleTracker = :smsSimpleTracker " +
						"AND message.number.id = :number " +
						"AND message.direction = :direction " +
						"AND message.route = smsSimpleTrackerRoute.route " +
						"AND smsSimpleTrackerRoute.smsSimpleTracker = :smsSimpleTracker")

				.setEntity (
					"smsSimpleTracker",
					smsSimpleTracker)

				.setEntity (
					"number",
					number)

				.setParameter (
					"direction",
					MessageDirection.out,
					MessageDirectionType.INSTANCE)

				.list ());

		}

	}

}
