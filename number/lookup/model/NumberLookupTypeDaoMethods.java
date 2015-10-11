package wbs.sms.number.lookup.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface NumberLookupTypeDaoMethods {

	List<NumberLookupTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}