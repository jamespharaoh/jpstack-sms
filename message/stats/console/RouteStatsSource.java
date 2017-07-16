package wbs.sms.message.stats.console;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.sms.object.stats.ObjectStatsSourceBuilder;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("routeStatsSource")
public
class RouteStatsSource
	implements ObjectStatsSourceBuilder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SmsStatsSourceImplementation> smsStatsSourceProvider;

	// implementation

	@Override
	public
	SmsStatsSource buildStatsSource (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"buildStatsSource");

		) {

			if (! ((Object) parent instanceof RouteRec))
				return null;

			return smsStatsSourceProvider.provide (
				transaction,
				smsStatsSource ->
					smsStatsSource

				.fixedCriteriaMap (
					ImmutableMap.of (
						SmsStatsCriteria.route,
						Collections.singleton (
							parent.getId ())))

			);

		}

	}

}
