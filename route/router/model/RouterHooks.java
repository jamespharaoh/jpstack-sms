package wbs.sms.route.router.model;

import static wbs.framework.utils.etc.Misc.doesNotContain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class RouterHooks
	implements ObjectHooks<RouterRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	RouterTypeDao routerTypeDao;

	// state

	Set<Integer> parentObjectTypeIds =
		new HashSet<Integer> ();

	// lifecycle

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

			List<RouterTypeRec> routerTypes =
				routerTypeDao.findByParentType (
					objectType);

			if (routerTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	@Override
	public
	void createSingletons (
			ObjectHelper<RouterRec> routerHelper,
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

		List<RouterTypeRec> routerTypes =
			routerTypeDao.findByParentType (
				parentType);

		for (
			RouterTypeRec routerType
				: routerTypes
		) {

			routerHelper.insert (
				routerHelper.createInstance ()

				.setRouterType (
					routerType)

				.setCode (
					routerType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}