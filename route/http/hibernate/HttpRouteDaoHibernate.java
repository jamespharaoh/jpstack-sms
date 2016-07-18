package wbs.sms.route.http.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.http.model.HttpRouteDao;
import wbs.sms.route.http.model.HttpRouteRec;

public
class HttpRouteDaoHibernate
	extends HibernateDao
	implements HttpRouteDao {

	@Override
	public
	HttpRouteRec find (
			@NonNull RouteRec route,
			@NonNull NetworkRec network) {

		return findOne (
			"find (route, network)",
			HttpRouteRec.class,

			createCriteria (
				HttpRouteRec.class,
				"_httpRoute")

			.add (
				Restrictions.eq (
					"_httpRoute.route",
					route))

			.add (
				Restrictions.eq (
					"_httpRoute.network",
					network))

		);

	}

}
