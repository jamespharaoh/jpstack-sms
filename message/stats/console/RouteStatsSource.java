package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("routeStatsSource")
public
class RouteStatsSource
	implements ObjectStatsSourceBuilder {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	Provider<SmsStatsSourceImpl> statsSourceImpl;

	@Override
	public
	SmsStatsSource buildStatsSource (
			Record<?> parent) {

		if (! ((Object) parent instanceof RouteRec))
			return null;

		return statsSourceImpl.get ()
			.fixedCriteriaMap (
				ImmutableMap.<SmsStatsCriteria,Set<Integer>>of (
					SmsStatsCriteria.route,
					Collections.singleton (
						parent.getId ())));

	}

}
