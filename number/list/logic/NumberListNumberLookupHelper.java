package wbs.sms.number.list.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListRec;
import wbs.sms.number.lookup.logic.NumberLookupHelper;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("numberListNumberLookupHelper")
public
class NumberListNumberLookupHelper
	implements NumberLookupHelper {

	// singleton dependencies

	@SingletonDependency
	NumberListLogic numberListLogic;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String parentObjectTypeCode () {
		return "number_list";
	}

	// implementation

	@Override
	public
	boolean lookupNumber (
			@NonNull NumberLookupRec numberLookup,
			@NonNull NumberRec number) {

		NumberListRec numberList =
			genericCastUnchecked (
				objectManager.getParentRequired (
					numberLookup));

		return numberListLogic.includesNumber (
			numberList,
			number);

	}

	@Override
	public
	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			@NonNull NumberLookupRec numberLookup,
			@NonNull List <NumberRec> numbers) {

		NumberListRec numberList =
			genericCastUnchecked (
				objectManager.getParentRequired (
					numberLookup));

		return numberListLogic.splitNumbersPresent (
			numberList,
			numbers);

	}

}
