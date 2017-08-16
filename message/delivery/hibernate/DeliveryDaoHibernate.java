package wbs.sms.message.delivery.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.message.delivery.model.DeliveryDao;
import wbs.sms.message.delivery.model.DeliveryRec;

@SingletonComponent ("deliveryDao")
public
class DeliveryDaoHibernate
	extends HibernateDaoLegacy
	implements DeliveryDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <DeliveryRec> findAllLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAllLimit");

		) {

			return findMany (
				transaction,
				DeliveryRec.class,

				createCriteria (
					transaction,
					DeliveryRec.class,
					"_delivery")

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

				.addOrder (
					Order.asc (
						"id"))

			);

		}

	}

}
