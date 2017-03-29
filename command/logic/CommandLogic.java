package wbs.sms.command.logic;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.sms.command.model.CommandRec;

public
interface CommandLogic {

	CommandRec findOrCreateCommand (
			TaskLogger parentTaskLogger,
			Record <?> parent,
			String typeCode,
			String code);

}
