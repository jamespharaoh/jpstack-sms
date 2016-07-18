package wbs.sms.messageset.model;

import java.util.List;

public
interface MessageSetTypeDaoMethods {

	MessageSetTypeRec findRequired (
			Long id);

	List<MessageSetTypeRec> findAll ();

}