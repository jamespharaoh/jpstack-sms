package wbs.sms.number.core.model;

import java.util.List;

public
interface NumberDaoMethods {

	List <Long> searchIds (
			NumberSearch numberSearch);

}