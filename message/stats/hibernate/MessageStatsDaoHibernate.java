package wbs.sms.message.stats.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.stats.model.MessageStatsDao;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.message.stats.model.MessageStatsSearch;

public
class MessageStatsDaoHibernate
	extends HibernateDao
	implements MessageStatsDao {

	@Override
	public
	List<MessageStatsRec> search (
			MessageStatsSearch search) {

		Criteria criteria =
			createCriteria (
				MessageStatsRec.class);

		if (search.dateAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"messageStatsId.date",
					search.dateAfter ()));

		}

		if (search.dateBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"messageStatsId.date",
					search.dateBefore ()));

		}

		if (search.routeId () != null) {

			criteria.add (
				Restrictions.eq (
					"messageStatsId.route.id",
					search.routeId ()));

		}

		if (search.routeIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"messageStatsId.route.id",
					search.routeIdIn ()));

		}

		if (search.serviceId () != null) {

			criteria.add (
				Restrictions.eq (
					"messageStatsId.service.id",
					search.serviceId ()));

		}

		if (search.serviceIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"messageStatsId.service.id",
					search.serviceIdIn ()));

		}

		if (search.affiliateId () != null) {

			criteria.add (
				Restrictions.eq (
					"messageStatsId.affiliate.id",
					search.affiliateId ()));

		}

		if (search.affiliateIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"messageStatsId.affiliate.id",
					search.affiliateIdIn ()));

		}

		if (search.batchId () != null) {

			criteria.add (
				Restrictions.eq (
					"messageStatsId.batch.id",
					search.batchId ()));

		}

		if (search.batchIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"messageStatsId.batch.id",
					search.batchIdIn ()));

		}

		if (search.networkId () != null) {

			criteria.add (
				Restrictions.eq (
					"messageStatsId.network.id",
					search.networkId ()));

		}

		if (search.networkIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"messageStatsId.network.id",
					search.networkIdIn ()));

		}

		if (search.filter ()) {

			criteria.add (
				Restrictions.or (

				Restrictions.in (
					"messageStatsId.service.id",
					search.filterServiceIds ()),

				Restrictions.in (
					"messageStatsId.affiliate.id",
					search.filterAffiliateIds ()),

				Restrictions.in (
					"messageStatsId.route.id",
					search.filterRouteIds ())));

		}

		return findMany (
			MessageStatsRec.class,
			criteria.list ());

	}

}
