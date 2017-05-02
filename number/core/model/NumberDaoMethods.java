package wbs.sms.number.core.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface NumberDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			NumberSearch numberSearch);

}