package wbs.sms.number.lookup.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("numberLookupManager")
public
class NumberLookupManagerImpl
	implements NumberLookupManager {

	// dependencies

	@Inject
	NumberLookupHelperManager numberLookupHelperManager;

	// implementation

	@Override
	public
	boolean lookupNumber (
			NumberLookupRec numberLookup,
			NumberRec number) {

		NumberLookupHelper helper =
			numberLookupHelperManager.forParentObjectTypeCode (
				numberLookup.getParentObjectType ().getCode (),
				true);

		return helper.lookupNumber (
			numberLookup,
			number);

	}

}
