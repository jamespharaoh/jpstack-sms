package wbs.sms.magicnumber.model;

import wbs.framework.database.Transaction;

import wbs.sms.command.model.CommandRec;
import wbs.sms.number.core.model.NumberRec;

public
interface MagicNumberUseDaoMethods {

	MagicNumberUseRec find (
			Transaction parentTransaction,
			MagicNumberRec magicNumber,
			NumberRec number);

	MagicNumberUseRec findExistingByRef (
			Transaction parentTransaction,
			MagicNumberSetRec magicNumberSet,
			NumberRec number,
			CommandRec command,
			Long ref);

	MagicNumberUseRec findExistingLeastRecentlyUsed (
			Transaction parentTransaction,
			MagicNumberSetRec magicNumberSet,
			NumberRec number);

}