package wbs.sms.number.lookup.logic;

import static wbs.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.number.lookup.model.NumberLookupRec;
import wbs.sms.number.lookup.model.NumberLookupTypeDao;
import wbs.sms.number.lookup.model.NumberLookupTypeRec;

public
class NumberLookupHooks
	implements ObjectHooks <NumberLookupRec> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeDao objectTypeDao;

	@SingletonDependency
	NumberLookupTypeDao numberLookupTypeDao;

	// state

	Map <Long, List <Long>> numberLookupTypeIdsByParentTypeId =
		new HashMap<> ();

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

			// preload object types

			objectTypeDao.findAll (
				transaction);

			// load number lookup types and construct index

			numberLookupTypeIdsByParentTypeId =
				numberLookupTypeDao.findAll (
					transaction)

				.stream ()

				.collect (
					Collectors.groupingBy (

					numberLookupType ->
						numberLookupType.getParentType ().getId (),

					Collectors.mapping (
						numberLookupType ->
							numberLookupType.getId (),
						Collectors.toList ())

				)

			);

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <NumberLookupRec> numberLookupHelper,
			@NonNull ObjectHelper <?> parentHelper,
			@NonNull Record <?> parent) {

		if (
			doesNotContain (
				numberLookupTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createSingletons");

		) {

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					transaction,
					parentHelper.objectTypeId ());

			for (
				Long numberLookupTypeId
					: numberLookupTypeIdsByParentTypeId.get (
						parentHelper.objectTypeId ())
			) {

				NumberLookupTypeRec numberLookupType =
					numberLookupTypeDao.findRequired (
						transaction,
						numberLookupTypeId);

				numberLookupHelper.insert (
					transaction,
					numberLookupHelper.createInstance ()

					.setParentType (
						parentType)

					.setParentId (
						parent.getId ())

					.setCode (
						numberLookupType.getCode ())

					.setNumberLookupType (
						numberLookupType)

				);

			}

		}

	}

}