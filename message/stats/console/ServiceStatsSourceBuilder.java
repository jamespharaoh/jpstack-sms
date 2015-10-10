package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("serviceStatsSourceBuilder")
public
class ServiceStatsSourceBuilder
	implements ObjectStatsSourceBuilder {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	Provider<SmsStatsSourceImpl> statsSourceImpl;

	// implementation

	@Override
	public
	SmsStatsSource buildStatsSource (
			Record<?> parent) {

		List<ServiceRec> services;

		if ((Object) parent instanceof ServiceRec) {

			services =
				Collections.singletonList (
					(ServiceRec)
					(Object)
					parent);

		} else {

			services =
				objectManager.getChildren (
					parent,
					ServiceRec.class);

		}

		if (services.isEmpty ())
			return null;

		for (
			ObjectHelper<?> objectHelper
				: objectManager.objectHelpers ()
		) {

			if (! objectHelper.major ())
				continue;

			if (! objectHelper.parentClass ().isInstance (parent))
				continue;

			List<? extends Record<?>> children =
				objectHelper.findByParent (
					parent);

			for (
				Record<?> child
					: children
			) {

				List<ServiceRec> childServices =
					objectManager.getChildren (
						child,
						ServiceRec.class);

				services.addAll (
					childServices);

			}

		}

		Set<Integer> serviceIds =
			new HashSet<Integer> ();

		for (
			ServiceRec service
				: services
		) {

			serviceIds.add (
				service.getId ());

		}

		return statsSourceImpl.get ()

			.fixedCriteriaMap (
				ImmutableMap.<SmsStatsCriteria,Set<Integer>>of (
					SmsStatsCriteria.service,
					serviceIds));

	}

}
