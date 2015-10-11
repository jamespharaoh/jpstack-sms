package wbs.sms.messageset.model;

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
class MessageSetHooks
	extends AbstractObjectHooks<MessageSetRec> {

	@Inject
	Database database;

	@Inject
	MessageSetTypeDao messageSetTypeDao;

	@Inject
	ObjectTypeDao objectTypeDao;

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

	@Override
	public
	void createSingletons (
			ObjectHelper<MessageSetRec> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ()))
			return;

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<MessageSetTypeRec> messageSetTypes =
			messageSetTypeDao.findByParentObjectType (
				parentType);

		for (MessageSetTypeRec messageSetType
				: messageSetTypes) {

			objectHelper.insert (
				new MessageSetRec ()
					.setMessageSetType (messageSetType)
					.setCode (messageSetType.getCode ())
					.setParentObjectType (parentType)
					.setParentObjectId (parent.getId ()));

		}

	}

}