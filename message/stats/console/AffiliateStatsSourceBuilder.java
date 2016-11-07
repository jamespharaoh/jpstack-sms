package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("affiliateStatsSourceBuilder")
public
class AffiliateStatsSourceBuilder
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
			Record<?> parent) {

		List<AffiliateRec> affiliates;

		if ((Object) parent instanceof AffiliateRec) {

			affiliates =
				Collections.singletonList (
					(AffiliateRec)
					parent);

		} else {

			affiliates =
				objectManager.getChildren (
					parent,
					AffiliateRec.class);

		}

		if (affiliates.isEmpty ())
			return null;

		Set<Long> affiliateIds =
			affiliates.stream ()

			.map (
				AffiliateRec::getId)

			.collect (
				Collectors.toSet ());

		return smsStatsSourceProvider.get ()

			.fixedCriteriaMap (
				ImmutableMap.of (
					SmsStatsCriteria.affiliate,
					affiliateIds));

	}

}
