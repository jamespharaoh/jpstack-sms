package wbs.sms.number.core.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelperMethods;
import wbs.sms.number.core.model.NumberRec;

public
class NumberObjectHelperMethodsImplementation
	implements NumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	NetworkObjectHelper networkHelper;

	@WeakSingletonDependency
	NumberObjectHelper numberHelper;

	// implementation

	@Override
	public
	NumberRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull String numberString) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// find existing

			Optional <NumberRec> numberRecordOptional =
				numberHelper.findByCode (
					transaction,
					GlobalId.root,
					numberString);

			if (
				optionalIsPresent (
					numberRecordOptional)
			) {
				return numberRecordOptional.get ();
			}

			// create it

			NetworkRec defaultNetwork =
				networkHelper.findRequired (
					transaction,
					0l);

			return numberHelper.insert (
				transaction,
				numberHelper.createInstance ()

				.setNumber (
					numberString)

				.setNetwork (
					defaultNetwork)

			);

		}

	}

	@Override
	public
	List <NumberRec> findOrCreateMany (
			@NonNull Transaction parentTransaction,
			@NonNull List <String> numberStrings) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreateMany");

		) {

			List <Optional <NumberRec>> numbersOptional =
				numberHelper.findManyByCode (
					transaction,
					GlobalId.root,
					numberStrings);

			ImmutableList.Builder <NumberRec> numbersBuilder =
				ImmutableList.builder ();

			NetworkRec defaultNetwork =
				networkHelper.findRequired (
					transaction,
					0l);

			for (
				long index = 0;
				index < collectionSize (numberStrings);
				index ++
			) {

				Optional <NumberRec> numberOptional =
					listItemAtIndexRequired (
						numbersOptional,
						index);

				if (
					optionalIsPresent (
						numberOptional)
				) {

					numbersBuilder.add (
						optionalGetRequired (
							numberOptional));

				} else {

					numbersBuilder.add (
						numberHelper.insert (
							transaction,
							numberHelper.createInstance ()

						.setNumber (
							listItemAtIndexRequired (
								numberStrings,
								index))

						.setNetwork (
							defaultNetwork)

					));

				}

			}

			return numbersBuilder.build ();

		}

	}

}