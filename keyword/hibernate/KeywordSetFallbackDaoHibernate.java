package wbs.sms.keyword.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull KeywordSetRec keywordSet,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"find (keywordSet, number)",
			KeywordSetFallbackRec.class,

			createCriteria (
				KeywordSetFallbackRec.class,
					"_keywordSetFallback")

			.add (
				Restrictions.eq (
					"_keywordSetFallback.keywordSet",
					keywordSet))

			.add (
				Restrictions.eq (
					"_keywordSetFallback.number",
					number))

		);

	}

}
