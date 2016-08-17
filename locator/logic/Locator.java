package wbs.sms.locator.logic;

import java.util.List;

import wbs.sms.locator.model.LongLat;

public
interface Locator {

	List<String> getTypeCodes ();

	LongLat lookup (
			Long locatorId,
			String number);

}
