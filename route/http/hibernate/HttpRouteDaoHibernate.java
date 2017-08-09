package wbs.sms.route.http.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.http.model.HttpRouteDao;
import wbs.sms.route.http.model.HttpRouteRec;

public
class HttpRouteDaoHibernate
	extends HibernateDao
	implements HttpRouteDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	HttpRouteRec find (
			@NonNull Transaction parentTransaction,
			@NonNull RouteRec route,
			@NonNull NetworkRec network) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				HttpRouteRec.class,

				createCriteria (
					transaction,
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

}
