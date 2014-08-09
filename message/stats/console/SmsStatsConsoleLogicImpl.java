package wbs.sms.message.stats.console;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("smsStatsConsoleLogic")
public
class SmsStatsConsoleLogicImpl
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
	PrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	public
	Map<SmsStatsCriteria,Set<Integer>> makeFilterMap () {

		if (privChecker.can (
				GlobalId.root,
				"stats"))
			return null;

		Set<Integer> serviceIds =
			new HashSet<Integer> ();

		serviceIds.add (-1);

		for (ServiceRec service
				: serviceHelper.findAll ()) {

			if (! privChecker.can (
					objectManager.getParent (service),
					"stats"))
				continue;

			serviceIds.add (
				service.getId ());

		}

		Set<Integer> affiliateIds =
			new HashSet<Integer> ();

		affiliateIds.add (-1);

		for (AffiliateRec affiliate
				: affiliateHelper.findAll ()) {

			if (privChecker.can (
					objectManager.getParent (affiliate),
					"stats")) {

				affiliateIds.add (
					affiliate.getId ());

			}

		}

		Set<Integer> routeIds =
			new HashSet<Integer> ();

		routeIds.add (-1);

		for (RouteRec route
				: routeHelper.findAll ()) {

			if (privChecker.can (route, "stats"))
				routeIds.add (route.getId ());

		}

		return ImmutableMap.<SmsStatsCriteria,Set<Integer>>builder ()
			.put (SmsStatsCriteria.service, serviceIds)
			.put (SmsStatsCriteria.affiliate, affiliateIds)
			.put (SmsStatsCriteria.route, routeIds)
			.build ();

	}

	@Override
	public
	String lookupGroupName (
			SmsStatsCriteria crit,
			int id) {

		switch (crit) {

		case route:

			return objectManager.objectPath (
				routeHelper.find (id),
				null,
				true,
				false);

		case service:

			return objectManager.objectPath (
				serviceHelper.find (id),
				null,
				true,
				false);

		case affiliate:

			return objectManager.objectPath (
				affiliateHelper.find (id),
				null,
				true,
				false);

		case network:

			NetworkRec network =
				networkHelper.find (id);

			return network.getDescription ();

		case batch:

			return batchHelper
				.find (id)
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
	public Map<SmsStatsCriteria,Set<Integer>> criteriaMapIntersect (
			Map<SmsStatsCriteria,
			Set<Integer>>... critMaps) {

		// start with an empty map (will return everything)

		Map<SmsStatsCriteria,Set<Integer>> ret =
			new HashMap<SmsStatsCriteria,Set<Integer>> ();

		// now intersect this with each map in turn

		for (Map<SmsStatsCriteria,Set<Integer>> critMap : critMaps) {

			// for each criterion to ids mapping...

			for (Map.Entry<SmsStatsCriteria,Set<Integer>> ent
					: critMap.entrySet ()) {

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
	public Map<String,Object> critMapToMessageStatsEntrySearchMap (
			Map<SmsStatsCriteria,Set<Integer>> critMap,
			Map<SmsStatsCriteria,Set<Integer>> filterMap) {

		Map<String,Object> ret =
			new HashMap<String, Object>();

		for (Map.Entry<SmsStatsCriteria,Set<Integer>> ent
				: critMap.entrySet ()) {

			ret.put (
				nameForCriteria (ent.getKey ()),
				new HashSet<Integer> (ent.getValue ()));

		}

		if (filterMap != null) {

			Map<String,Object> retFilter =
				new HashMap<String,Object> ();

			retFilter.put (
				"serviceIds",
				new HashSet<Integer> (
					filterMap.get (SmsStatsCriteria.service)));

			retFilter.put (
				"affiliateIds",
				new HashSet<Integer> (
					filterMap.get (SmsStatsCriteria.affiliate)));

			retFilter.put (
				"routeIds",
				new HashSet<Integer> (
					filterMap.get (SmsStatsCriteria.route)));

			ret.put (
				"filter",
				retFilter);

		}

		return ret;
	}

	@Override
	public String nameForCriteria (
			SmsStatsCriteria statsCriteria) {

		switch (statsCriteria) {

		case route:
			return "routeIdIn";

		case service:
			return "serviceIdIn";

		case affiliate:
			return "affiliateIdIn";

		case batch:
			return "batchIdIn";

		case network:
			return "networkIdIn";

		}

		throw new IllegalArgumentException ();

	}

}
