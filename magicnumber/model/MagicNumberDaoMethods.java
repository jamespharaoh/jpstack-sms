package wbs.sms.magicnumber.model;

import wbs.sms.number.core.model.NumberRec;

public
interface MagicNumberDaoMethods {

	MagicNumberRec findByNumber (
			String number);

	MagicNumberRec findExistingUnused (
			MagicNumberSetRec magicNumberSet,
			NumberRec number);

}