package wbs.sms.message.delivery.hibernate;

import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import org.hibernate.criterion.Order;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.delivery.model.DeliveryDao;
import wbs.sms.message.delivery.model.DeliveryRec;

@SingletonComponent ("deliveryDao")
public
class DeliveryDaoHibernate
	extends HibernateDao
	implements DeliveryDao {

	@Override
	public
	List <DeliveryRec> findAllLimit (
			@NonNull Long maxResults) {

		return findMany (
			"findAllLimit (maxResults)",
			DeliveryRec.class,

			createCriteria (
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
