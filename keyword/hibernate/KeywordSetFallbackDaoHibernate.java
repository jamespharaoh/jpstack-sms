package wbs.sms.keyword.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.keyword.model.KeywordSetFallbackDao;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

public
class KeywordSetFallbackDaoHibernate
	extends HibernateDao
	implements KeywordSetFallbackDao {

	@Override
	public
	KeywordSetFallbackRec find (
			KeywordSetRec keywordSet,
			NumberRec number) {

		return findOne (
			KeywordSetFallbackRec.class,

			createQuery (
				"FROM KeywordSetFallbackRec keywordSetFallback " +
				"WHERE keywordSetFallback.keywordSet = :keywordSet " +
					"AND keywordSetFallback.number = :number")

			.setEntity (
				"keywordSet",
				keywordSet)

			.setEntity (
				"number",
				number)

			.list ());

	}

}
