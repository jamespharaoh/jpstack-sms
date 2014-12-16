package wbs.sms.command.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeObjectHelper;
import wbs.sms.command.model.CommandTypeRec;

@SingletonComponent ("commandLogic")
public
class CommandLogicImpl
	implements CommandLogic {

	// dependencies

	@Inject
	CommandTypeObjectHelper commandTypeHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	ObjectManager objectManager;

	// implementation

	@Override
	public
	CommandRec findOrCreateCommand (
			Record<?> parent,
			String typeCode,
			String code) {

		// lookup existing command...

		CommandRec command =
			objectManager.findChildByCode (
				CommandRec.class,
				parent,
				code);

		if (command != null)
			return command;

		// ...or create new command

		ObjectTypeRec parentType =
			objectTypeHelper.find (
				objectManager.getObjectTypeId (parent));

		CommandTypeRec commandType =
			commandTypeHelper.findByCode (
				parentType,
				typeCode);

		return objectManager.insert (
			new CommandRec ()

			.setCode (
				code)

			.setCommandType (
				commandType)

			.setParentObjectType (
				parentType)

			.setParentObjectId (
				parent.getId ())

		);

	}

}
