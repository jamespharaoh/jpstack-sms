package wbs.sms.number.lookup.logic;

import java.util.Map;

import javax.inject.Inject;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("numberLookupHelperManager")
public
class NumberLookupHelperManager
	extends AbstractHelperManager<NumberLookupHelper> {

	// collection dependencies

	@Inject
	Map<String,NumberLookupHelper> numberLookupHelpersByBeanName;

	// details

	@Override
	public
	Map<String,NumberLookupHelper> helpersByBeanName () {
		return numberLookupHelpersByBeanName;
	}

	@Override
	public
	String friendlyName () {
		return "number lookup";
	}

}
