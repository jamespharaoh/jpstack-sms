package wbs.sms.command.logic;

import wbs.framework.record.Record;
import wbs.sms.command.model.CommandRec;

public
interface CommandLogic {

	// TODO move into helper
	CommandRec findOrCreateCommand (
			Record<?> parent,
			String typeCode,
			String code);

}
