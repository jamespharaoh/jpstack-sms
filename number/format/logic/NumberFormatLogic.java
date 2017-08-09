package wbs.sms.number.format.logic;

import java.util.List;

import wbs.sms.number.format.model.NumberFormatRec;

public
interface NumberFormatLogic {

	String parse (
			NumberFormatRec numberFormat,
			String number)
		throws WbsNumberFormatException;

	List<String> parseLines (
			NumberFormatRec numberFormat,
			String numbers)
		throws WbsNumberFormatException;

}
