package wbs.sms.command.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.command.model.CommandTypeDao;
import wbs.sms.command.model.CommandTypeRec;

public
class CommandTypeDaoHibernate
	extends HibernateDaoLegacy
	implements CommandTypeDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <CommandTypeRec> findAll (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAll");

		) {

			return findMany (
				transaction,
				CommandTypeRec.class,

				createCriteria (
					transaction,
					CommandTypeRec.class)

			);

		}

	}

	@Override
	public
	CommandTypeRec findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Long commandTypeId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRequired");

		) {

			return get (
				transaction,
				CommandTypeRec.class,
				commandTypeId);

		}

	}

}
