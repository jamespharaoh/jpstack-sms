package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.moreThan;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.console.helper.AbstractConsoleHooks;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("messageConsoleHooks")
public
class MessageConsoleHooks
	extends AbstractConsoleHooks<MessageRec> {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	// implementation

	@Override
	public
	Optional<String> getListClass (
			MessageRec message) {

		if (
			equal (
				message.getDirection (),
				MessageDirection.in)
		) {

			return Optional.of (
				"message-in");

		} else if (
			moreThan (
				message.getCharge (),
				0)
		) {

			return Optional.of (
				"message-out-charge");

		} else {

			return Optional.of (
				"message-out");

		}

	}

	@Override
	public
	void applySearchFilter (
			Object searchObject) {

		MessageSearch search =
			(MessageSearch)
			searchObject;

		search

			.filter (
				true);

		// services

		ImmutableList.Builder<Integer> servicesBuilder =
			ImmutableList.<Integer>builder ();

		for (
			ServiceRec service
				: serviceHelper.findAll ()
		) {

			Record<?> serviceParent =
				objectManager.getParent (
					service);

			 if (
			 	! privChecker.can (
			 		serviceParent,
			 		"messages")
			 ) {
			 	continue;
			 }

			servicesBuilder.add (
				service.getId ());

		}

		search

			.filterServiceIds (
				servicesBuilder.build ());

		// affiliates

		ImmutableList.Builder<Integer> affiliatesBuilder =
			ImmutableList.<Integer>builder ();

		for (
			AffiliateRec affiliate
				: affiliateHelper.findAll ()
		) {

			Record<?> affiliateParent =
				objectManager.getParent (
					affiliate);

			if (
				! privChecker.can (
					affiliateParent,
					"messages")
			) {
				continue;
			}

			affiliatesBuilder.add (
				affiliate.getId ());

		}

		search

			.filterAffiliateIds (
				affiliatesBuilder.build ());

		// routes

		ImmutableList.Builder<Integer> routesBuilder =
			ImmutableList.<Integer>builder ();

		for (
			RouteRec route
				: routeHelper.findAll ()
		) {

			if (
				! privChecker.can (
					route,
					"messages")
			) {
				continue;
			}

			routesBuilder.add (
				route.getId ());

		}

		search

			.filterRouteIds (
				routesBuilder.build ());

	}

}
