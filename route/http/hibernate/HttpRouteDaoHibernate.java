package wbs.sms.route.http.hibernate;

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
			RouteRec route,
			NetworkRec network) {

		return findOne (
			HttpRouteRec.class,

			createQuery (
				"FROM HttpRouteRec httpRoute " +
				"WHERE httpRoute.route = :route " +
					"AND httpRoute.network = :network")

			.setEntity (
				"route",
				route)

			.setEntity (
				"network",
				network)

			.list ());

	}

}
