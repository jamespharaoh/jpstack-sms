package wbs.sms.number.core.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class NumberHooks
	extends AbstractObjectHooks<NumberRec> {

	// dependencies

	@Inject
	NumberDao numberDao;

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		NumberSearch numberSearch =
			(NumberSearch) search;

		return numberDao.searchIds (
			numberSearch);

	}

}