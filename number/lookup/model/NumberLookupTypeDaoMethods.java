package wbs.sms.number.lookup.model;

import java.util.List;

public
interface NumberLookupTypeDaoMethods {

	NumberLookupTypeRec findRequired (
			Long id);

	List<NumberLookupTypeRec> findAll ();

}