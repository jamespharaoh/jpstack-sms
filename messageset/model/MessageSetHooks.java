package wbs.sms.messageset.model;

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
class MessageSetHooks
	implements ObjectHooks<MessageSetRec> {

	// dependencies

	@Inject
	Database database;

	@Inject
	MessageSetTypeDao messageSetTypeDao;

	@Inject
	ObjectTypeDao objectTypeDao;

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

		for (ObjectTypeRec objectType : objectTypes) {

			List<MessageSetTypeRec> messageSetTypes =
				messageSetTypeDao.findByParentObjectType (
					objectType);

			if (messageSetTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			ObjectHelper<MessageSetRec> messageSetHelper,
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

		List<MessageSetTypeRec> messageSetTypes =
			messageSetTypeDao.findByParentObjectType (
				parentType);

		for (
			MessageSetTypeRec messageSetType
				: messageSetTypes
		) {

			messageSetHelper.insert (
				messageSetHelper.createInstance ()

				.setMessageSetType (
					messageSetType)

				.setCode (
					messageSetType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}