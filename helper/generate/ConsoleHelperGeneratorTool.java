package wbs.console.helper.generate;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.keyEqualsString;

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
class ConsoleHelperGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleHelperGenerator> consoleHelperGeneratorProvider;

	// implementation

	public
	void generateConsoleHelpers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> params) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"generateConsoleHelpers");

		) {

			List <Model <?>> models =
				entityHelper.recordModels ();

			taskLogger.noticeFormat (
				"About to generate %s console helpers",
				integerToDecimalString (
					collectionSize (
						models)));

			long numSuccess = 0;
			long numFailures = 0;

			for (
				Model <?> model
					: models
			) {

				try (

					OwnedTaskLogger nestedTaskLogger =
						logContext.nestTaskLogger (
							taskLogger,
							"generateConsoleHelpers.loop",
							keyEqualsString (
								"model.objectName",
								model.objectName ()));

				) {

					consoleHelperGeneratorProvider.provide (
						nestedTaskLogger)

						.model (
							model)

						.generateHelper (
							nestedTaskLogger);

					if (nestedTaskLogger.errors ()) {

						nestedTaskLogger.errorFormat (
							"Error writing console helper for %s",
							model.objectName ());

						numFailures ++;

					} else {

						numSuccess ++;

					}

				}

			}

			taskLogger.noticeFormat (
				"Successfully generated %s console helpers",
				integerToDecimalString (
					numSuccess));

			if (
				moreThanZero (
					numFailures)
			) {

				taskLogger.errorFormat (
					"Failed to generate %s console helpers",
					integerToDecimalString (
						numFailures));

			}

		}

	}

}
