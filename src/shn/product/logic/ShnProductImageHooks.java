package shn.product.logic;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.listSlice;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.IterableUtils.iterableChainToList;
import static wbs.utils.collection.IterableUtils.iterableFindFirst;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import shn.product.model.ShnProductImageRec;
import shn.product.model.ShnProductRec;

public
class ShnProductImageHooks
	implements ObjectHooks <ShnProductImageRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ShnProductLogic productLogic;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductImageRec productImage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ShnProductRec product =
				productImage.getProduct ();

			for (
				long index = product.getNumImagesNotDeleted ();
				index < product.getNumImages ();
				index ++
			) {

				ShnProductImageRec productImageNested =
					listItemAtIndexRequired (
						product.getImages (),
						index);

				productImageNested

					.setIndex (
						productImageNested.getIndex () + 1)

				;

			}

			productImage

				.setCreateTime (
					transaction.now ())

				.setUpdateTime (
					transaction.now ())

				.setIndex (
					product.getNumImagesNotDeleted ())

				.setNormalisedMedia (
					productLogic.normaliseImage (
						transaction,
						productImage.getOriginalMedia ()))

				.setNormaliseTime (
					transaction.now ())

			;

			product

				.setImages (
					iterableChainToList (
						ImmutableList.of (
							listSlice (
								product.getImages (),
								0l,
								product.getNumImagesNotDeleted ()),
							singletonList (
								productImage),
							listSlice (
								product.getImages (),
								product.getNumImagesNotDeleted (),
								product.getNumImages ()))))

				.setNumImages (
					product.getNumImages () + 1)

				.setNumImagesNotDeleted (
					product.getNumImagesNotDeleted () + 1)

				.setPrimaryImage (
					optionalOrNull (
						iterableFindFirst (
							product.getImages (),
							productImageNested ->
								! productImageNested.getDeleted ()
								&& productImageNested.getActive ())))

				.setUpdateTime (
					transaction.now ())

				.setShopifyNeedsSync (
					true)

			;

		}

	}

	@Override
	public
	void beforeUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductImageRec productImage) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeUpdate");

		) {

			ShnProductRec product =
				productImage.getProduct ();

			productImage

				.setUpdateTime (
					transaction.now ())

			;

			product

				.setUpdateTime (
					transaction.now ())

				.setShopifyNeedsSync (
					true)

			;

		}

	}

}
