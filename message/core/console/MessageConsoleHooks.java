package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.NumberUtils.moreThanZero;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import wbs.console.helper.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;
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
	implements ConsoleHooks<MessageRec> {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	// implementation

	@Override
	public
	Optional <String> getListClass (
			@NonNull MessageRec message) {

		if (
			enumEqualSafe (
				message.getDirection (),
				MessageDirection.in)
		) {

			return Optional.of (
				"message-in");

		} else if (
			moreThanZero (
				message.getCharge ())
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

		ImmutableList.Builder<Long> servicesBuilder =
			ImmutableList.builder ();

		for (
			ServiceRec service
				: serviceHelper.findAll ()
		) {

			Record<?> serviceParent =
				objectManager.getParent (
					service);

			 if (
			 	! privChecker.canRecursive (
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

		ImmutableList.Builder<Long> affiliatesBuilder =
			ImmutableList.builder ();

		for (
			AffiliateRec affiliate
				: affiliateHelper.findAll ()
		) {

			Record<?> affiliateParent =
				objectManager.getParent (
					affiliate);

			if (
				! privChecker.canRecursive (
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

		ImmutableList.Builder<Long> routesBuilder =
			ImmutableList.builder ();

		for (
			RouteRec route
				: routeHelper.findAll ()
		) {

			if (
				! privChecker.canRecursive (
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
