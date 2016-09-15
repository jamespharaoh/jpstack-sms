package wbs.sms.message.stats.console;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("batchStatsSourceBuilder")
public
class BatchStatsSourceBuilder
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

		List<BatchRec> batches =
			objectManager.getChildren (
				parent,
				BatchRec.class);

		if (batches.isEmpty ())
			return null;

		Set<Long> batchIds =
			new HashSet<> ();

		for (
			BatchRec batch
				: batches
		) {

			batchIds.add (
				batch.getId ());

		}

		return smsStatsSourceProvider.get ()

			.fixedCriteriaMap (
				ImmutableMap.of (
					SmsStatsCriteria.batch,
					batchIds));

	}

}
