package wbs.sms.number.list.model;

public
interface NumberListNumberDaoMethods {

	NumberListNumberRec findByNumberListAndNumber (
			int numberListId,
			int numberId);

}