package wbs.sms.number.lookup.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class NumberLookupHooks
	implements ObjectHooks<NumberLookupRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	NumberLookupTypeDao numberLookupTypeDao;

	// state

	Map<Long,List<Long>> numberLookupTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"numberLookupHooks.init ()",
				this);

		// preload object types

		objectTypeDao.findAll ();

		// load number lookup types and construct index

		numberLookupTypeIdsByParentTypeId =
			numberLookupTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (

				numberLookupType ->
					numberLookupType.getParentType ().getId (),

				Collectors.mapping (
					numberLookupType ->
						numberLookupType.getId (),
					Collectors.toList ())

			));

	}

	// implementation

	@Override
	public
	void createSingletons (
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