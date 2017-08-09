package wbs.sms.command.logic;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.sms.command.model.CommandRec;

public
interface CommandLogic {

	CommandRec findOrCreateCommand (
			Transaction parentTransaction,
			Record <?> parent,
			String typeCode,
			String code);

}
