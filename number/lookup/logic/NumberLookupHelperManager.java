package wbs.sms.number.lookup.logic;

import java.util.Map;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("numberLookupHelperManager")
public
class NumberLookupHelperManager
	extends AbstractHelperManager <NumberLookupHelper> {

	// singleton dependencies

	@SingletonDependency
	Map <String, NumberLookupHelper> numberLookupHelpersByBeanName;

	// details

	@Override
	public
	Map <String, NumberLookupHelper> helpersByBeanName () {
		return numberLookupHelpersByBeanName;
	}

	@Override
	public
	String friendlyName () {
		return "number lookup";
	}

}
