package wbs.sms.route.router.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface RouterTypeDaoMethods {

	RouterTypeRec findRequired (
			Transaction parentTransaction,
			Long id);

	List <RouterTypeRec> findAll (
			Transaction parentTransaction);

}