package wbs.sms.message.stats.console;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("batchStatsSourceBuilder")
public
class BatchStatsSourceBuilder
	implements ObjectStatsSourceBuilder {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<SmsStatsSourceImplementation> smsStatsSourceProvider;

	// implementation

	@Override
	public
	SmsStatsSource buildStatsSource (
			Record<?> parent) {

		List<BatchRec> batches =
			objectManager.getChildren (
				parent,
				BatchRec.class);

		if (batches.isEmpty ())
			return null;

		Set<Integer> batchIds =
			new HashSet<Integer> ();

		for (
			BatchRec batch
				: batches
		) {

			batchIds.add (
				batch.getId ());

		}

		return smsStatsSourceProvider.get ()

			.fixedCriteriaMap (
				ImmutableMap.<SmsStatsCriteria,Set<Integer>>of (
					SmsStatsCriteria.batch,
					batchIds));

	}

}
