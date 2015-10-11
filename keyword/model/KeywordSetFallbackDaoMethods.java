package wbs.sms.keyword.model;

import wbs.sms.number.core.model.NumberRec;

public
interface KeywordSetFallbackDaoMethods {

	KeywordSetFallbackRec find (
			KeywordSetRec keywordSet,
			NumberRec number);

}