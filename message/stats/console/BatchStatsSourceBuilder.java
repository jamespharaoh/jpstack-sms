package wbs.sms.message.stats.console;

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

import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.object.stats.ObjectStatsSourceBuilder;

@SingletonComponent ("batchStatsSourceBuilder")
public
class BatchStatsSourceBuilder
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

			List <BatchRec> batches =
				objectManager.getChildren (
					transaction,
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

}
