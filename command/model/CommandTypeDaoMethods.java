package wbs.sms.command.model;

import java.util.List;

public
interface CommandTypeDaoMethods {

	CommandTypeRec findRequired (
			Long id);

	List<CommandTypeRec> findAll ();

}