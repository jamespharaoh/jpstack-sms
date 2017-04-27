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
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String numberString) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrCreate");

		) {

			// find existing

			Optional <NumberRec> numberRecordOptional =
				numberHelper.findByCode (
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
					0l);

			return numberHelper.insert (
				taskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> numberStrings) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrCreateMany");

		) {

			List <Optional <NumberRec>> numbersOptional =
				numberHelper.findManyByCode (
					GlobalId.root,
					numberStrings);

			ImmutableList.Builder <NumberRec> numbersBuilder =
				ImmutableList.builder ();

			NetworkRec defaultNetwork =
				networkHelper.findRequired (
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
							taskLogger,
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