package wbs.sms.route.router.model;

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

	Map<Long,List<Long>> routerTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@PostConstruct
	public
	void init () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"routerHooks.init ()",
				this);

		routerTypeIdsByParentTypeId =
			routerTypeDao.findAll ().stream ()

			.collect (
				Collectors.groupingBy (
					routerType -> (long)
						routerType.getParentType ().getId (),
					Collectors.mapping (
						routerType -> (long)
							routerType.getId (),
						Collectors.toList ())));

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull ObjectHelper<RouterRec> routerHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				routerTypeIdsByParentTypeId.keySet (),
				(long) parentHelper.objectTypeId ())
		) {
			return;
		}

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long routerTypeId
				: routerTypeIdsByParentTypeId.get (
					(long) parentHelper.objectTypeId ())
		) {

			RouterTypeRec routerType =
				routerTypeDao.findRequired (
					routerTypeId);

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