package wbs.sms.number.list.logic;

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
			NumberLookupRec numberLookup,
			NumberRec number) {

		NumberListRec numberList =
			(NumberListRec)
			objectManager.getParent (
				numberLookup);

		return numberListLogic.includesNumber (
			numberList,
			number);

	}

}
