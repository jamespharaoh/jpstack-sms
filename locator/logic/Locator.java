package wbs.sms.locator.logic;

import java.util.List;

import wbs.framework.logging.TaskLogger;

import wbs.sms.locator.model.LongLat;

public
interface Locator {

	List <String> getTypeCodes ();

	LongLat lookup (
			TaskLogger parentTaskLogger,
			Long locatorId,
			String number);

}
