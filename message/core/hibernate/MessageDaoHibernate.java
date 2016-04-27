package wbs.sms.message.core.hibernate;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageDao;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("messageDao")
public
class MessageDaoHibernate
	extends HibernateDao
	implements MessageDao {

	// implementation

	@Override
	public
	List<MessageRec> findNotProcessed () {

		return findMany (
			MessageRec.class,

			createQuery (
				"FROM MessageRec message " +
				"WHERE message.status = 7 " +
				"ORDER BY message.createdTime DESC")

			.list ());

	}

	@Override
	public
	long countNotProcessed () {

		return findOne (
			Long.class,

			createQuery (
				"SELECT count (*) " +
				"FROM MessageRec message " +
				"WHERE message.status = 7")

			.list ());

	}

	@Override
	public
	MessageRec findByOtherId (
			@NonNull MessageDirection direction,
			@NonNull RouteRec route,
			@NonNull String otherId) {

		return findOne (
			MessageRec.class,

			createQuery (
				"FROM MessageRec message " +
				"WHERE message.direction = :direction " +
					"AND message.route = :route " +
					"AND message.otherId = :otherId")

			.setParameter (
				"direction",
				direction,
				MessageDirectionType.INSTANCE)

			.setEntity (
				"route",
				route)

			.setString (
				"otherId",
				otherId)

			.list ());

	}

	@Override
	public
	List<MessageRec> findByThreadId (
			long threadId) {

		return findMany (
			MessageRec.class,

			createQuery (
				"FROM MessageRec message " +
				"WHERE message.threadId = :threadId")

			.setInteger (
				"threadId",
				(int) (long)
				threadId)

			.list ());

	}

	@Override
	public
	List<MessageRec> findRecentLimit (
			long maxResults) {

		return findMany (
			MessageRec.class,

			createCriteria (
				MessageRec.class)

			.addOrder (
				Order.desc ("id"))

			.setMaxResults (
				(int) maxResults)

			.list ());

	}

	@Override
	public
	List<ServiceRec> projectServices (
			NumberRec number) {

		return findMany (
			ServiceRec.class,

			createCriteria (
				MessageRec.class,
				"_message")

			.createAlias (
				"service",
				"_service")

			.add (
				Restrictions.eq (
					"_message.number",
					number))

			.setProjection (
				Projections.projectionList ()

					.add (
						Projections.groupProperty (
							"_message.service")))

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			MessageSearch search) {

		Criteria criteria =
			createCriteria (
				MessageRec.class)

			.createAlias (
				"network",
				"network",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"number",
				"number",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"route",
				"route",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"service",
				"service",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"affiliate",
				"affiliate",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"batch",
				"batch",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"text",
				"text",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"user",
				"user",
				JoinType.LEFT_OUTER_JOIN);

		if (search.messageId () != null) {

			criteria.add (
				Restrictions.eq (
					"id",
					search.messageId ()));

		}

		if (search.number () != null) {

			criteria.add (
				Restrictions.ilike (
					"number.number",
					search.number ()));

		}

		if (search.numberId () != null) {

			criteria.add (
				Restrictions.eq (
					"number.id",
					search.numberId ()));

		}

		if (search.serviceId () != null) {

			criteria.add (
				Restrictions.eq (
					"service.id",
					search.serviceId ()));

		}

		if (search.serviceIdIn () != null) {

			if (search.serviceIdIn ().isEmpty ())
				return Collections.emptyList ();

			criteria.add (
				Restrictions.in (
					"service.id",
					search.serviceIdIn ()));

		}

		if (search.affiliateId () != null) {

			criteria.add (
				Restrictions.eq (
					"affiliate.id",
					search.affiliateId ()));

		}

		if (search.affiliateIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"affiliate.id",
					search.affiliateIdIn ()));

		}

		if (search.batchId () != null) {

			criteria.add (
				Restrictions.eq (
					"batch.id",
					search.batchId ()));

		}

		if (search.batchIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"batch.id",
					search.batchIdIn ()));

		}

		if (search.routeId () != null) {

			criteria.add (
				Restrictions.eq (
					"route.id",
					search.routeId ()));

		}

		if (search.routeIdIn () != null) {

			criteria.add (
				Restrictions.in (
					"route.id",
					search.routeIdIn ()));

		}

		if (search.networkId () != null) {

			criteria.add (
				Restrictions.eq (
					"network.id",
					search.networkId ()));
		}

		if (search.status () != null) {

			criteria.add (
				Restrictions.eq (
					"status",
					search.status ()));

		}

		if (
			isNotNull (
				search.createdTime ())
		) {

			criteria.add (
				Restrictions.ge (
					"createdTime",
					search.createdTime ().start ()));

			criteria.add (
				Restrictions.lt (
					"createdTime",
					search.createdTime ().end ()));

		}

		if (
			isNotNull (
				search.createdTimeAfter ())
		) {

			criteria.add (
				Restrictions.ge (
					"createdTime",
					search.createdTimeAfter ()));

		}

		if (
			isNotNull (
				search.createdTimeBefore ())
		) {

			criteria.add (
				Restrictions.lt (
					"createdTime",
					search.createdTimeBefore ()));

		}

		if (
			isNotNull (
				search.direction ())
		) {

			criteria.add (
				Restrictions.eq (
					"direction",
					search.direction ()));

		}

		if (
			isNotNull (
				search.statusIn ())
		) {

			criteria.add (
				Restrictions.in (
					"status",
					search.statusIn ()));

		}

		if (
			isNotNull (
				search.statusNotIn ())
		) {

			criteria.add (
				Restrictions.not (
					Restrictions.in (
						"status",
						search.statusNotIn ())));

		}

		if (
			isNotNull (
				search.textLike ())
		) {

			criteria.add (
				Restrictions.like (
					"text.text",
					search.textLike ()));

		}

		if (
			isNotNull (
				search.textILike ())
		) {

			criteria.add (
				Restrictions.ilike (
					"text.text",
					search.textILike ()));

		}

		if (
			isNotNull (
				search.textContains ())
		) {

			criteria.add (
				Restrictions.ilike (
					"text.text",
					stringFormat (
						"%%%s%%",
						search.textContains ())));

		}

		if (
			isNotNull (
				search.userId ())
		) {

			criteria.add (
				Restrictions.eq (
					"user.id",
					search.userId ()));

		}

		if (
			isNotNull (
				search.maxResults ())
		) {

			criteria.setMaxResults (
				search.maxResults ());

		}

		switch (
			ifNull (
				search.orderBy (),
				MessageSearchOrder.createdTimeDesc)
		) {

		case createdTime:

			criteria.addOrder (
				Order.asc ("createdTime"));

			break;

		case createdTimeDesc:

			criteria.addOrder (
				Order.desc ("createdTime"));

			break;

		default:

			throw new IllegalArgumentException (
				search.orderBy ().toString ());

		}

		if (search.filter ()) {

			List<Criterion> filterCriteria =
				new ArrayList<Criterion> ();

			if (
				isNotEmpty (
					search.filterServiceIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"service.id",
						search.filterServiceIds ()));

			}

			if (
				isNotEmpty (
					search.filterAffiliateIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"affiliate.id",
						search.filterAffiliateIds ()));

			}

			if (
				isNotEmpty (
					search.filterRouteIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"route.id",
						search.filterRouteIds ()));

			}

			criteria.add (
				Restrictions.or (
					filterCriteria.toArray (
						new Criterion [] {})));

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			criteria.list ());

	}

}
