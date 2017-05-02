package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("serviceStatsSourceBuilder")
public
class ServiceStatsSourceBuilder
	implements ObjectStatsSourceBuilder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsStatsSourceImplementation> smsStatsSourceProvider;

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

			List <ServiceRec> services;

			if (parent instanceof ServiceRec) {

				services =
					Collections.singletonList (
						(ServiceRec)
						parent);

			} else {

				services =
					objectManager.getChildren (
						transaction,
						parent,
						ServiceRec.class);

			}

			if (services.isEmpty ())
				return null;

			for (
				ObjectHelper<?> objectHelper
					: objectManager.objectHelpers ()
			) {

				if (
					! objectHelper.major ()
					|| objectHelper.isRoot ()
					|| ! objectHelper.parentClass ().isInstance (parent)
				) {
					continue;
				}

				List <? extends Record <?>> children =
					objectHelper.findByParent (
						transaction,
						parent);

				for (
					Record <?> child
						: children
				) {

					List <ServiceRec> childServices =
						objectManager.getChildren (
							transaction,
							child,
							ServiceRec.class);

					services.addAll (
						childServices);

				}

			}

			Set<Long> serviceIds =
				new HashSet<> ();

			for (
				ServiceRec service
					: services
			) {

				serviceIds.add (
					service.getId ());

			}

			return smsStatsSourceProvider.get ()

				.fixedCriteriaMap (
					ImmutableMap.of (
						SmsStatsCriteria.service,
						serviceIds));

		}

	}

}
