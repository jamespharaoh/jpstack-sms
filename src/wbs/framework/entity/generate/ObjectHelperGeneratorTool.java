package wbs.framework.entity.generate;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class ObjectHelperGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ObjectHelperGenerator> objectHelperGeneratorProvider;

	// implementation

	public
	void generateObjectHelpers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> params) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"generateObjectHelpers");

		) {

			List <Model <?>> models =
				entityHelper.recordModels ();

			taskLogger.noticeFormat (
				"About to generate %s object helpers",
				integerToDecimalString (
					collectionSize (
						models)));

			long numSuccess = 0;
			long numFailures = 0;

			for (
				Model <?> model
					: models
			) {

				try {

					objectHelperGeneratorProvider.provide (
						taskLogger)

						.model (
							model)

						.generateHelper (
							taskLogger);

					numSuccess ++;

				} catch (Exception exception) {

					taskLogger.errorFormatException (
						exception,
						"Error writing object helper for %s",
						model.objectName ());

					numFailures ++;

				}

			}

			taskLogger.noticeFormat (
				"Successfully generated %s object helpers",
				integerToDecimalString (
					numSuccess));

			if (numFailures > 0) {

				taskLogger.errorFormat (
					"Aborting due to %s errors",
					integerToDecimalString (
						numFailures));

			}

		}

	}

}
