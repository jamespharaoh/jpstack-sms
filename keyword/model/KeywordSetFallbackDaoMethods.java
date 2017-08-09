package wbs.sms.keyword.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface KeywordSetFallbackDaoMethods {

	KeywordSetFallbackRec find (
			Transaction parentTransaction,
			KeywordSetRec keywordSet,
			NumberRec number);

}