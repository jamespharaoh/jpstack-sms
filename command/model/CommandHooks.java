package wbs.sms.command.model;

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
class CommandHooks
	extends AbstractObjectHooks<CommandRec> {

	// dependencies

	@Inject
	CommandTypeDao commandTypeDao;

	@Inject
	Database database;

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

			List<CommandTypeRec> commandTypes =
				commandTypeDao.findByParentObjectType (
					objectType);

			if (commandTypes.isEmpty ())
				continue;

			parentObjectTypeIds.add (
				objectType.getId ());

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			ObjectHelper<CommandRec> commandHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (! parentObjectTypeIds.contains (
				parentHelper.objectTypeId ()))
			return;

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		List<CommandTypeRec> commandTypes =
			commandTypeDao.findByParentObjectType (
				parentType);

		for (CommandTypeRec commandType
				: commandTypes) {

			commandHelper.insert (
				new CommandRec ()
					.setCommandType (commandType)
					.setCode (commandType.getCode ())
					.setParentObjectType (parentType)
					.setParentObjectId (parent.getId ()));

		}

	}

}