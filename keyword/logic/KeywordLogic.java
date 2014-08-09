package wbs.sms.keyword.logic;

import wbs.sms.command.model.CommandRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

public
interface KeywordLogic {

	boolean checkKeyword (
			String keyword);

	void createOrUpdateKeywordSetFallback (
			KeywordSetRec keywordSet,
			NumberRec number,
			CommandRec command);

}
