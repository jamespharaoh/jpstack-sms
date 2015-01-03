package wbs.sms.number.lookup.logic;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

public
interface NumberLookupHelper
	extends Helper {

	@Override
	String parentObjectTypeCode ();

	boolean lookupNumber (
			NumberLookupRec numberLookup,
			NumberRec number);

}
