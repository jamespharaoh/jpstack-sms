package wbs.sms.message.stats.console;

import java.util.Collections;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("routeStatsSource")
public
class RouteStatsSource
	implements ObjectStatsSourceBuilder {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsStatsSourceImplementation> smsStatsSourceProvider;

	// implementation

	@Override
	public
	SmsStatsSource buildStatsSource (
			@NonNull Record<?> parent) {

		if (! ((Object) parent instanceof RouteRec))
			return null;

		return smsStatsSourceProvider.get ()

			.fixedCriteriaMap (
				ImmutableMap.of (
					SmsStatsCriteria.route,
					Collections.singleton (
						parent.getId ())));

	}

}
