package wbs.sms.message.core.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
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
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("messageDao")
public
class MessageDaoHibernate
	extends HibernateDao
	implements MessageDao {

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
	int countNotProcessed () {

		return (int) (long) findOne (
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
			int threadId) {

		return findMany (
			MessageRec.class,

			createQuery (
				"FROM MessageRec message " +
				"WHERE message.threadId = :threadId")

			.setInteger (
				"threadId",
				threadId)

			.list ());

	}

	@Override
	public
	List<MessageRec> findRecentLimit (
			int maxResults) {

		return findMany (
			MessageRec.class,

			createCriteria (
				MessageRec.class)

			.addOrder (
				Order.desc ("id"))

			.setMaxResults (
				maxResults)

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
	List<MessageRec> search (
			MessageSearch search) {

		Criteria criteria =
			createCriteria (MessageRec.class)

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

		if (search.id () != null) {

			criteria.add (
				Restrictions.eq (
					"id",
					search.id ()));

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

		if (search.createdTimeAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"createdTime",
					instantToDate (
						search.createdTimeAfter ())));

		}

		if (search.createdTimeBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"createdTime",
					instantToDate (
						search.createdTimeBefore ())));

		}

		if (search.direction () != null) {

			criteria.add (
				Restrictions.eq (
					"direction",
					search.direction ()));

		}

		if (search.statusIn () != null) {

			criteria.add (
				Restrictions.in (
					"status",
					search.statusIn ()));

		}

		if (search.statusNotIn () != null) {

			criteria.add (
				Restrictions.not (
					Restrictions.in (
						"status",
						search.statusNotIn ())));

		}

		if (search.textLike () != null) {

			criteria.add (
				Restrictions.like (
					"text.text",
					search.textLike ()));

		}

		if (search.textILike () != null) {

			criteria.add (
				Restrictions.ilike (
					"text.text",
					search.textILike ()));

		}

		if (search.userId () != null) {

			criteria.add (
				Restrictions.eq (
					"user.id",
					search.userId ()));

		}

		if (search.maxResults () != null) {

			criteria.setMaxResults (
				search.maxResults ());

		}

		if (search.orderBy () != null) {

			switch (search.orderBy ()) {

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

		}

		if (search.filter ()) {

			criteria.add (
				Restrictions.or (

					Restrictions.in ("service.id",
						search.filterServiceIds ()),

					Restrictions.in ("affiliate.id",
						search.filterAffiliateIds ()),

					Restrictions.in ("route.id",
						search.filterRouteIds ())));

		}

		return findMany (
			MessageRec.class,
			criteria.list ());

	}

}
