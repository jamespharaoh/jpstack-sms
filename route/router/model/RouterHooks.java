package wbs.sms.route.router.model;

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
class RouterHooks
	extends AbstractObjectHooks<RouterRec> {

	@Inject
	Database database;

	@Inject
	ObjectTypeDao objectTypeDao;

	@Inject
	RouterDao routerDao;

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

			List<RouterTypeRec> routerTypes =
				routerDao.findByParentObjectType (
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

		if (! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ()))
			return;

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<RouterTypeRec> routerTypes =
			routerDao.findByParentObjectType (
				parentType);

		for (RouterTypeRec routerType
				: routerTypes) {

			routerHelper.insert (
				new RouterRec ()

				.setRouterType (
					routerType)

				.setCode (
					routerType.getCode ())

				.setParentObjectType (
					parentType)

				.setParentObjectId (
					parent.getId ())

			);

		}

	}

}