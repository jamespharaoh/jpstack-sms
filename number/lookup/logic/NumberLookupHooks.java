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
	void init () {

		try (

			Transaction transaction =
				database.beginReadOnly (
					"numberLookupHooks.init ()",
					this);

		) {

			// preload object types

			objectTypeDao.findAll ();

			// load number lookup types and construct index

			numberLookupTypeIdsByParentTypeId =
				numberLookupTypeDao.findAll ().stream ().collect (
					Collectors.groupingBy (

				numberLookupType ->
					numberLookupType.getParentType ().getId (),

				Collectors.mapping (
					numberLookupType ->
						numberLookupType.getId (),
					Collectors.toList ())

			));

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper<NumberLookupRec> numberLookupHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				numberLookupTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSingletons");

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long numberLookupTypeId
				: numberLookupTypeIdsByParentTypeId.get (
					parentHelper.objectTypeId ())
		) {

			NumberLookupTypeRec numberLookupType =
				numberLookupTypeDao.findRequired (
					numberLookupTypeId);

			numberLookupHelper.insert (
				taskLogger,
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