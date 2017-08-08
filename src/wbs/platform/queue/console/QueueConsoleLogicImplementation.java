package wbs.platform.queue.console;

import static wbs.utils.collection.IterableUtils.iterableFilterMapToSet;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.LogicUtils.predicatesCombineAll;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.metamodel.QueueTypeSpec;
import wbs.platform.queue.metamodel.QueueTypesSpec;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.scaffold.logic.SliceLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("queueConsoleLogic")
public
class QueueConsoleLogicImplementation
	implements QueueConsoleLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	@SingletonDependency
	SliceLogic sliceLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueConsoleHelper queueHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	Map <String, QueueTypeSpec> queueTypeSpecs;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"setup");

		) {

			// collect queue type definitions

			ImmutableMap.Builder <String, QueueTypeSpec> queueTypeSpecsBuilder =
				ImmutableMap.builder ();

			for (
				RecordSpec modelMeta
					: modelMetaLoader.recordSpecs ().values ()
			) {

				for (
					Object childObject
						: modelMeta.children ()
				) {

					if (
						isNotInstanceOf (
							QueueTypesSpec.class,
							childObject)
					) {
						continue;
					}

					QueueTypesSpec queueTypes =
						(QueueTypesSpec)
						childObject;

					for (
						QueueTypeSpec queueType
							: queueTypes.queueTypes ()
					) {

						String queueTypeName =
							joinWithFullStop (
								camelToUnderscore (
									modelMeta.name ()),
								simplifyToCodeRequired (
									queueType.name ()));

						queueTypeSpecsBuilder.put (
							queueTypeName,
							queueType);

					}

				}

			}

			queueTypeSpecs =
				queueTypeSpecsBuilder.build ();

		}

	}

	// implementation

	@Override
	public
	QueueTypeSpec queueTypeSpec (
			@NonNull Transaction parentTransaction,
			@NonNull QueueTypeRec queueType) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"queueTypeSpec");

		) {

			String queueTypeName =
				joinWithFullStop (
					queueType.getParentType ().getCode (),
					queueType.getCode ());

			QueueTypeSpec queueTypeSpec =
				queueTypeSpecs.get (
					queueTypeName);

			if (
				isNull (
					queueTypeSpec)
			) {

				throw new RuntimeException (
					stringFormat (
						"No such queue type spec: %s",
						queueTypeName));

			}

			return queueTypeSpec;

		}

	}

	@Override
	public
	QueueItemRec claimQueueItem (
			@NonNull Transaction parentTransaction,
			@NonNull QueueRec queue,
			@NonNull UserRec user) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"claimQueueItem");

		) {

			// find the next waiting item

			SortedQueueSubjects subjects =
				queueSubjectSorterProvider.provide (
					transaction)

				.queueCache (
					dummyQueueCache)

				.queue (
					queue)

				.loggedInUser (
					userConsoleLogic.userRequired (
						transaction))

				.effectiveUser (
					user)

				.sort (
					transaction)

			;

			if (subjects.availableSubjects ().isEmpty ()) {
				return null;
			}

			QueueSubjectRec queueSubject =
				subjects.availableSubjects ().get (0).subject ();

			long nextQueueItemId =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec queueItem =
				queueItemHelper.findByIndexRequired (
					transaction,
					queueSubject,
					nextQueueItemId);

			// sanity checks

			if (queueItem.getState () != QueueItemState.pending)
				throw new IllegalStateException ();

			if (queueItem.getQueueItemClaim () != null)
				throw new IllegalStateException ();

			// create queue item claim

			QueueItemClaimRec queueItemClaim =
				queueItemClaimHelper.insert (
					transaction,
					queueItemClaimHelper.createInstance ()

				.setQueueItem (
					queueItem)

				.setUser (
					user)

				.setStartTime (
					transaction.now ())

				.setStatus (
					QueueItemClaimStatus.claimed)

			);

			// update queue item

			queueItem

				.setState (
					QueueItemState.claimed)

				.setQueueItemClaim (
					queueItemClaim);

			// update slice

			sliceLogic.updateSliceInactivityTimestamp (
				transaction,
				user.getSlice (),
				optionalAbsent ());

			// and return

			return queueItem;

		}

	}

	@Override
	public
	void unclaimQueueItem (
			@NonNull Transaction parentTransaction,
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec user) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"unclaimQueueItem");

		) {

			QueueSubjectRec queueSubject =
				queueItem.getQueueSubject ();

			// sanity checks

			long currentItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			if (queueItem.getIndex () != currentItemIndex) {
				throw new IllegalStateException ();
			}

			if (queueItem.getState () != QueueItemState.claimed) {

				throw new RuntimeException (
					stringFormat (
						"Cannot unclaim queue item in %s state",
						enumName (
							queueItem.getState ())));

			}

			if (queueItem.getQueueItemClaim ().getUser () != user) {

				throw new RuntimeException (
					"Trying to unclaim item belonging to another user");

			}

			// update queue item claim

			queueItem.getQueueItemClaim ()

				.setEndTime (
					transaction.now ())

				.setStatus (
					QueueItemClaimStatus.unclaimed);

			// update the queue item

			queueItem

				.setState (
					QueueItemState.pending)

				.setQueueItemClaim (
					null);

			// update slice

			sliceLogic.updateSliceInactivityTimestamp (
				transaction,
				user.getSlice (),
				optionalAbsent ());

		}

	}

	@Override
	public
	void reclaimQueueItem (
			@NonNull Transaction parentTransaction,
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec oldUser,
			@NonNull UserRec newUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"reclaimQueueItem");

		) {

			QueueSubjectRec queueSubject =
				queueItem.getQueueSubject ();

			// sanity checks

			long currentItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			if (queueItem.getIndex () != currentItemIndex)
				throw new IllegalStateException ();

			if (queueItem.getState () != QueueItemState.claimed) {

				throw new IllegalStateException (
					stringFormat (
						"Cannot reclaim queue item in %s state",
						enumName (
							queueItem.getState ())));

			}

			if (queueItem.getQueueItemClaim ().getUser () != oldUser) {

				throw new IllegalStateException (
					"Item being reclaimed does not belong to expected user");

			}

			// update old queue item claim

			queueItem.getQueueItemClaim ()

				.setEndTime (
					transaction.now ())

				.setStatus (
					QueueItemClaimStatus.forcedUnclaim);

			// create new queue item claim

			QueueItemClaimRec queueItemClaim =
				queueItemClaimHelper.insert (
					transaction,
					queueItemClaimHelper.createInstance ()

				.setQueueItem (
					queueItem)

				.setUser (
					newUser)

				.setStartTime (
					transaction.now ())

				.setStatus (
					QueueItemClaimStatus.claimed)

			);

			// update queue item

			queueItem

				.setQueueItemClaim (
					queueItemClaim);

			// update slice

			sliceLogic.updateSliceInactivityTimestamp (
				transaction,
				newUser.getSlice (),
				optionalAbsent ());

		}

	}

	@Override
	public
	boolean canSupervise (
			@NonNull Transaction parentTransaction,
			@NonNull QueueRec queue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canSupervise");

		) {

			QueueTypeSpec queueTypeSpec =
				queueTypeSpec (
					transaction,
					queue.getQueueType ());

			String[] supervisorParts =
				queueTypeSpec.supervisorPriv ().split (":");

			Record <?> supervisorDelegate =
				genericCastUnchecked (
					objectManager.dereferenceRequired (
						transaction,
						queue,
						supervisorParts [0]));

			return privChecker.canRecursive (
				transaction,
				supervisorDelegate,
				supervisorParts [1]);

		}

	}

	@Override
	public
	Set <Long> getSupervisorSearchIds (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorSearchIds");

		) {

			List <Predicate <QueueRec>> predicates =
				new ArrayList<> ();

			// handle slice code condition

			Optional <Set <String>> sliceCodesOptional =
				mapItemForKey (
					conditions,
					"slice-code");

			if (
				optionalIsPresent (
					sliceCodesOptional)
			) {

				Set <String> sliceCodes =
					optionalGetRequired (
						sliceCodesOptional);

				predicates.add (
					queue ->
						contains (
							sliceCodes,
							queue.getSlice ().getCode ()));

			}

			// handle manual responder slice code condition

			Optional <Set <String>> queueSliceCodesOptional =
				mapItemForKey (
					conditions,
					"queue-slice-code");

			if (
				optionalIsPresent (
					queueSliceCodesOptional)
			) {

				Set <String> queueSliceCodes =
					optionalGetRequired (
						queueSliceCodesOptional);

				predicates.add (
					queue ->
						contains (
							queueSliceCodes,
							queue.getSlice ().getCode ()));

			}

			// handle chat code condition

			Optional <Set <String>> queueTypeCodesOptional =
				mapItemForKey (
					conditions,
					"queue-type-code");

			if (
				optionalIsPresent (
					queueTypeCodesOptional)
			) {

				Set <String> queueTypeCodes =
					optionalGetRequired (
						queueTypeCodesOptional);

				predicates.add (
					queue ->
						contains (
							queueTypeCodes,
							joinWithFullStop (
								queue.getQueueType ().getParentType ().getCode (),
								queue.getQueueType ().getCode ())));

			}

			// return

			return iterableFilterMapToSet (
				queueHelper.findAll (
					transaction),
				predicatesCombineAll (
					predicates),
				QueueRec::getId);

		}

	}

	@Override
	public
	Set <Long> getSupervisorFilterIds (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSupervisorFilterUserIds");

		) {

			return iterableFilterMapToSet (
				queueHelper.findAll (
					transaction),
				queue ->
					privChecker.canRecursive (
						transaction,
						new GlobalId (
							queue.getParentType ().getId (),
							queue.getParentId ()),
						"supervisor"),
				QueueRec::getId);

		}

	}

}
