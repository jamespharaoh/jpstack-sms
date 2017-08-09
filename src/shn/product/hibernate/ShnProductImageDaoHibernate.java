package shn.product.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductImageDaoMethods;
import shn.product.model.ShnProductImageRec;

public
class ShnProductImageDaoHibernate
	extends HibernateDao
	implements ShnProductImageDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <Long> findIdsPendingImageNormalisationLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findIdsPendingImageNormalisationLimit");

		) {

			return findMany (
				transaction,
				Long.class,

				createCriteria (
					transaction,
					ShnProductImageRec.class,
					"_productImage")

				.createAlias (
					"_productImage.product",
					"_product")

				.createAlias (
					"_product.database",
					"_database")

				.add (
					Restrictions.ltProperty (
						"_productImage.normaliseTime",
						"_database.imageNormaliseTime"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

				.setProjection (
					Projections.id ())

			);

		}

	}

}
