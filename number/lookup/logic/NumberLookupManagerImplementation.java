package wbs.sms.number.lookup.logic;

import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

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

	@Override
	public
	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			@NonNull NumberLookupRec numberLookup,
			@NonNull List <NumberRec> numbers) {

		NumberLookupHelper helper =
			numberLookupHelperManager.forParentObjectTypeCode (
				numberLookup.getParentType ().getCode (),
				true);

		return helper.splitNumbersPresent (
				numberLookup,
				numbers);

	}

}
