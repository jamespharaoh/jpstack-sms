package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.entity.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("affiliateStatsSourceBuilder")
public
class AffiliateStatsSourceBuilder
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

		return smsStatsSource.get ()

			.fixedCriteriaMap (
				ImmutableMap.of (
					SmsStatsCriteria.affiliate,
					affiliateIds));

	}

}
