package wbs.sms.messageset.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface MessageSetTypeDaoMethods {

	MessageSetTypeRec findRequired (
			Transaction parentTransaction,
			Long id);

	List <MessageSetTypeRec> findAll (
			Transaction parentTransaction);

}