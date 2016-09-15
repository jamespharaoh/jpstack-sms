package wbs.sms.number.lookup.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("numberLookupManager")
public
class NumberLookupManagerImplementation
	implements NumberLookupManager {

	// singleton dependencies

	@SingletonDependency
	NumberLookupHelperManager numberLookupHelperManager;

	// implementation

	@Override
	public
	boolean lookupNumber (
			@NonNull NumberLookupRec numberLookup,
			@NonNull NumberRec number) {

		NumberLookupHelper helper =
			numberLookupHelperManager.forParentObjectTypeCode (
				numberLookup.getParentType ().getCode (),
				true);

		return helper.lookupNumber (
			numberLookup,
			number);

	}

}
