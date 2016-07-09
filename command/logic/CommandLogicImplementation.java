package wbs.sms.command.logic;

import static wbs.framework.utils.etc.Misc.isPresent;

import javax.inject.Inject;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeObjectHelper;
import wbs.sms.command.model.CommandTypeRec;

@SingletonComponent ("commandLogic")
public
class CommandLogicImplementation
	implements CommandLogic {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

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

		Optional<CommandRec> existingCommandOptional =
			commandHelper.findByCode (
				parent,
				code);

		if (
			isPresent (
				existingCommandOptional)
		) {
			return existingCommandOptional.get ();
		}

		// ...or create new command

		ObjectTypeRec parentType =
			objectTypeHelper.findRequired (
				objectManager.getObjectTypeId (
					parent));

		CommandTypeRec commandType =
			commandTypeHelper.findByCodeRequired (
				parentType,
				typeCode);

		return commandHelper.insert (
			commandHelper.createInstance ()

			.setCode (
				code)

			.setCommandType (
				commandType)

			.setParentType (
				parentType)

			.setParentId (
				parent.getId ())

		);

	}

}
