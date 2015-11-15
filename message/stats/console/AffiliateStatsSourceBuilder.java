package wbs.sms.message.stats.console;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
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
					(Object)
					parent);

		} else {

			affiliates =
				objectManager.getChildren (
					parent,
					AffiliateRec.class);

		}

		if (affiliates.isEmpty ())
			return null;

		Set<Integer> affiliateIds =
			new HashSet<Integer> ();

		for (AffiliateRec affiliate
				: affiliates) {

			affiliateIds.add (
				affiliate.getId ());

		}

		return smsStatsSource.get ()
			.fixedCriteriaMap (
				ImmutableMap.<SmsStatsCriteria,Set<Integer>>of (
					SmsStatsCriteria.affiliate,
					affiliateIds));

	}

}
