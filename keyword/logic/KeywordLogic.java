package wbs.sms.keyword.logic;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.sms.command.model.CommandRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

public
interface KeywordLogic {

	boolean checkKeyword (
			TaskLogger parentTaskLogger,
			String keyword);

	void createOrUpdateKeywordSetFallback (
			Transaction parentTransaction,
			KeywordSetRec keywordSet,
			NumberRec number,
			CommandRec command);

}
