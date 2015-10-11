package wbs.sms.magicnumber.model;

import wbs.sms.command.model.CommandRec;
import wbs.sms.number.core.model.NumberRec;

public
interface MagicNumberUseDaoMethods {

	MagicNumberUseRec find (
			MagicNumberRec magicNumber,
			NumberRec number);

	MagicNumberUseRec findExistingByRef (
			MagicNumberSetRec magicNumberSet,
			NumberRec number,
			CommandRec command,
			Integer ref);

	MagicNumberUseRec findExistingLeastRecentlyUsed (
			MagicNumberSetRec magicNumberSet,
			NumberRec number);

}