package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.ResultUtils.getError;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import fj.data.Either;

@SingletonComponent ("messageConsoleHooks")
@Log4j
public
class MessageConsoleHooks
	implements ConsoleHooks <MessageRec> {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
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

			Either <Optional <Record <?>>, String> serviceParentOrError =
				objectManager.getParentOrError (
					service);

			if (
				isError (
					serviceParentOrError)
			) {

				log.warn (
					getError (
						serviceParentOrError));

				continue;

			}

			Record <?> serviceParent =
				optionalGetRequired (
					resultValueRequired (
						serviceParentOrError));

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

			Either <Optional <Record <?>>, String> affiliateParentOrError =
				objectManager.getParentOrError (
					affiliate);

			if (
				isError (
					affiliateParentOrError)
			) {

				log.warn (
					getError (
						affiliateParentOrError));

				continue;

			}

			Record <?> affiliateParent =
				optionalGetRequired (
					resultValueRequired (
						affiliateParentOrError));

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
