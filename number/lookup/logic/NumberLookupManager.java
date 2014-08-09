package wbs.sms.number.lookup.logic;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

public
interface NumberLookupManager {

	boolean lookupNumber (
			NumberLookupRec numberLookup,
			NumberRec number);

}
