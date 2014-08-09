package wbs.sms.message.stats.hibernate;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.stats.model.MessageStatsDao;
import wbs.sms.message.stats.model.MessageStatsRec;

public
class MessageStatsDaoHibernate
	extends HibernateDao
	implements MessageStatsDao {

	@Override
	public
	List<MessageStatsRec> search (
			Map<String,Object> searchMap) {

		Criteria crit =
			createCriteria (MessageStatsRec.class);

		for (Map.Entry<String,Object> entry
				: searchMap.entrySet ()) {

			String key =
				entry.getKey ();

			Object value =
				entry.getValue ();

			if (equal (key, "dateAfter")) {

				crit.add (
					Restrictions.ge (
						"messageStatsId.date",
						value));

			} else if (equal (key, "dateBefore")) {

				crit.add (
					Restrictions.lt (
						"messageStatsId.date",
						value));

			} else if (equal (key, "routeId")) {

				crit.add (
					Restrictions.eq (
						"messageStatsId.route.id",
						(Integer) value));

			} else if (equal (key, "routeIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.route.id",
						(Collection<?>) value));

			} else if (equal (key, "serviceId")) {

				crit.add (
					Restrictions.eq (
						"messageStatsId.service.id",
						(Integer) value));

			} else if (equal (key, "serviceIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.service.id",
						(Collection<?>) value));

			} else if (equal (key, "affiliateIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.affiliate.id",
						(Collection<?>) value));

			} else if (equal (key, "affiliateId")) {

				crit.add (
					Restrictions.eq (
						"messageStatsId.affiliate.id",
						(Integer) value));

			} else if (equal (key, "batchIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.batch.id",
						(Collection<?>) value));

			} else if (equal (key, "batchId")) {

				crit.add (
					Restrictions.eq (
						"messageStatsId.batch.id",
						(Integer) value));

			} else if (equal (key, "networkIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.network.id",
						(Collection<?>) value));

			} else if (equal (key, "revenueIdIn")) {

				crit.add (
					Restrictions.in (
						"messageStatsId.network.id",
						(Collection<?>) value));

			} else if (equal (key, "networkId")) {

				crit.add (
					Restrictions.eq (
						"messageStatsId.network.id",
						(Integer) value));

			} else if (equal (key, "filter")) {

				@SuppressWarnings ("unchecked")
				Map<String,Collection<?>> map =
					(Map<String,Collection<?>>) value;

				crit.add (Restrictions.or (

					Restrictions.in (
						"messageStatsId.service.id",
						map.get ("serviceIds")),

					Restrictions.in (
						"messageStatsId.affiliate.id",
						map.get ("affiliateIds")),

					Restrictions.in (
						"messageStatsId.route.id",
						map.get ("routeIds"))));

			} else {

				throw new IllegalArgumentException (
					"Unrecognised search key " + key);

			}

		}

		return findMany (
			MessageStatsRec.class,
			crit.list ());

	}

}
