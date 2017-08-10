package wbs.sms.messageset.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.messageset.model.MessageSetTypeDao;
import wbs.sms.messageset.model.MessageSetTypeRec;

public
class MessageSetTypeDaoHibernate
	extends HibernateDaoLegacy
	implements MessageSetTypeDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <MessageSetTypeRec> findAll (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAll");

		) {

			return findMany (
				transaction,
				MessageSetTypeRec.class,

				createCriteria (
					transaction,
					MessageSetTypeRec.class)

			);

		}

	}

	@Override
	public
	MessageSetTypeRec findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Long messageSetTypeId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRequired");

		) {

			return get (
				transaction,
				MessageSetTypeRec.class,
				messageSetTypeId);

		}

	}

}
