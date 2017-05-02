package wbs.sms.number.lookup.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface NumberLookupTypeDaoMethods {

	NumberLookupTypeRec findRequired (
			Transaction parentTransaction,
			Long id);

	List <NumberLookupTypeRec> findAll (
			Transaction parentTransaction);

}