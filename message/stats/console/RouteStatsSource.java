package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("routeStatsSource")
public
class RouteStatsSource
	implements ObjectStatsSourceBuilder {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<SmsStatsSourceImplementation> smsStatsSource;

	// implementation

	@Override
	public
	SmsStatsSource buildStatsSource (
			@NonNull Record<?> parent) {

		if (! ((Object) parent instanceof RouteRec))
			return null;

		return smsStatsSource.get ()

			.fixedCriteriaMap (
				ImmutableMap.<SmsStatsCriteria,Set<Integer>>of (
					SmsStatsCriteria.route,
					Collections.singleton (
						parent.getId ())));

	}

}
