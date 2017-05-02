package wbs.sms.magicnumber.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface MagicNumberDaoMethods {

	MagicNumberRec findByNumber (
			Transaction parentTransaction,
			String number);

	MagicNumberRec findExistingUnused (
			Transaction parentTransaction,
			MagicNumberSetRec magicNumberSet,
			NumberRec number);

}