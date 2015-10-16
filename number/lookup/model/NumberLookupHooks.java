package wbs.sms.number.lookup.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class NumberLookupHooks
	extends AbstractObjectHooks<NumberLookupRec> {

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	NumberLookupTypeDao numberLookupTypeDao;

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ObjectTypeRec> objectTypes =
			objectTypeDao.findAll ();

		for (
			ObjectTypeRec objectType
				: objectTypes
		) {

			List<NumberLookupTypeRec> numberLookupTypes =
				numberLookupTypeDao.findByParentObjectType (
					objectType);

			if (numberLookupTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	@Override
	public
	void createSingletons (
			ObjectHelper<NumberLookupRec> numberLookupHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (
			doesNotContain (
				parentObjectTypeIds,
				parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<NumberLookupTypeRec> numberLookupTypes =
			numberLookupTypeDao.findByParentObjectType (
				parentType);

		for (
			NumberLookupTypeRec numberLookupType
				: numberLookupTypes
		) {

			numberLookupHelper.insert (
				new NumberLookupRec ()

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