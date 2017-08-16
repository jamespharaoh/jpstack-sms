package wbs.sms.keyword.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.keyword.model.KeywordSetFallbackDao;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

public
class KeywordSetFallbackDaoHibernate
	extends HibernateDaoLegacy
	implements KeywordSetFallbackDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	KeywordSetFallbackRec find (
			@NonNull Transaction parentTransaction,
			@NonNull KeywordSetRec keywordSet,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				KeywordSetFallbackRec.class,

				createCriteria (
					transaction,
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

}
