package wbs.sms.command.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface CommandTypeDaoMethods {

	CommandTypeRec findRequired (
			Transaction parentTransaction,
			Long id);

	List <CommandTypeRec> findAll (
			Transaction parentTransaction);

}