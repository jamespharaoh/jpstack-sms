package wbs.sms.message.stats.console;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.stats.model.MessageStatsSearch;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("smsStatsConsoleLogic")
@Log4j
public
class SmsStatsConsoleLogicImplementation
	implements SmsStatsConsoleLogic {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	NetworkConsoleHelper networkHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	public
	Map<SmsStatsCriteria,Set<Integer>> makeFilterMap () {

		if (privChecker.canRecursive (
				GlobalId.root,
				"stats"))
			return null;

		Set<Integer> serviceIds =
			new HashSet<Integer> ();

		serviceIds.add (-1);

		for (
			ServiceRec service
				: serviceHelper.findAll ()
		) {

			try {

				if (! privChecker.canRecursive (
						objectManager.getParent (service),
						"stats"))
					continue;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error checking privs for service %s",
						service.getId ()),
					exception);

				continue;

			}

			serviceIds.add (
				service.getId ());

		}

		Set<Integer> affiliateIds =
			new HashSet<Integer> ();

		affiliateIds.add (-1);

		for (
			AffiliateRec affiliate
				: affiliateHelper.findAll ()
		) {

			try {

				if (! privChecker.canRecursive (
						objectManager.getParent (affiliate),
						"stats"))
					continue;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error checking privs for affiliate %s",
						affiliate.getId ()),
					exception);

				continue;

			}

			affiliateIds.add (
				affiliate.getId ());

		}

		Set<Integer> routeIds =
			new HashSet<Integer> ();

		routeIds.add (-1);

		for (
			RouteRec route
				: routeHelper.findAll ()
		) {

			if (
				privChecker.canRecursive (
					route,
					"stats")
			) {

				routeIds.add (
					route.getId ());

			}

		}

		return ImmutableMap.<SmsStatsCriteria,Set<Integer>>builder ()

			.put (
				SmsStatsCriteria.service,
				serviceIds)

			.put (
				SmsStatsCriteria.affiliate,
				affiliateIds)

			.put (
				SmsStatsCriteria.route,
				routeIds)

			.build ();

	}

	@Override
	public
	String lookupGroupName (
			@NonNull SmsStatsCriteria crit,
			int id) {

		switch (crit) {

		case route:

			return objectManager.objectPathMini (
				routeHelper.findOrNull (id));

		case service:

			return objectManager.objectPathMini (
				serviceHelper.findOrNull (id));

		case affiliate:

			return objectManager.objectPathMini (
				affiliateHelper.findOrNull (id));

		case network:

			NetworkRec network =
				networkHelper.findOrNull (id);

			return network.getDescription ();

		case batch:

			return batchHelper
				.findOrNull (id)
				.getId ()
				.toString ();

		}

		throw new IllegalArgumentException ();

	}

	/**
	 * Computes the intersection of the two critMaps, that is the critMap which
	 * will only return items that would have been returned by both critMaps.
	 */
	@Override
	public
	Map<SmsStatsCriteria,Set<Integer>> criteriaMapIntersect (
			@NonNull List<Map<SmsStatsCriteria,Set<Integer>>> critMaps) {

		// start with an empty map (will return everything)

		Map<SmsStatsCriteria,Set<Integer>> ret =
			new HashMap<SmsStatsCriteria,Set<Integer>> ();

		// now intersect this with each map in turn

		for (
			Map<SmsStatsCriteria,Set<Integer>> critMap
				: critMaps
		) {

			// for each criterion to ids mapping...

			for (
				Map.Entry<SmsStatsCriteria,Set<Integer>> ent
					: critMap.entrySet ()
			) {

				SmsStatsCriteria crit =
					ent.getKey ();

				Collection<Integer> ids =
					ent.getValue ();

				// if this criterion is already listed...

				if (ret.containsKey (crit)) {

					// ...intersect it (remove any ids not listed in this map
					// too)

					ret.get (crit).retainAll (ids);

				} else {

					// ...otherwise just copy this restriction

					ret.put (
						crit,
						new HashSet<Integer> (ids));

				}

			}

		}

		return ret;

	}

	/**
	 * Combines a critMap and an optional filterMap into a searchMap suitable
	 * for passing to coreDao.searchMessageStatsEntries().
	 */
	@Override
	public
	MessageStatsSearch critMapToMessageStatsSearch (
			Map<SmsStatsCriteria,Set<Integer>> critMap,
			Optional<Map<SmsStatsCriteria,Set<Integer>>> filterMap) {

		MessageStatsSearch search =
			new MessageStatsSearch ();

		for (
			Map.Entry<SmsStatsCriteria,Set<Integer>> ent
				: critMap.entrySet ()
		) {

			setSearchCriteria (
				search,
				ent.getKey (),
				ent.getValue ());

		}

		if (
			isPresent (
				filterMap)
		) {

			search

				.filter (
					true)

				.filterServiceIds (
					filterMap.get ().get (
						SmsStatsCriteria.service))

				.filterAffiliateIds (
					filterMap.get ().get (
						SmsStatsCriteria.affiliate))

				.filterRouteIds (
					filterMap.get ().get (
						SmsStatsCriteria.route));

		}

		return search;

	}

	@Override
	public
	MessageStatsSearch setSearchCriteria (
			MessageStatsSearch search,
			SmsStatsCriteria statsCriteria,
			Collection<Integer> value) {

		switch (statsCriteria) {

		case route:

			return search.routeIdIn (
				value);

		case service:

			return search.serviceIdIn (
				value);

		case affiliate:

			return search.affiliateIdIn (
				value);

		case batch:

			return search.batchIdIn (
				value);

		case network:

			return search.networkIdIn (
				value);

		}

		throw new IllegalArgumentException ();

	}

}
