package wbs.framework.entity.generate;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

import com.google.common.collect.Iterables;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class ModelGeneratorTool {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ModelRecordGenerator> modelRecordGeneratorProvider;

	@PrototypeDependency
	ComponentProvider <ModelInterfacesGenerator>
		modelInterfacesGeneratorProvider;

	// implementation

	public
	void generateModels (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> params) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"generateModels");

		) {

			taskLogger.noticeFormat (
				"About to generate %s models",
				integerToDecimalString (
					modelMetaLoader.allModelMetas ().size ()));

			StatusCounters statusCounters =
				new StatusCounters ();

			for (
				RecordSpec modelMeta
					: Iterables.concat (
						modelMetaLoader.modelMetas ().values (),
						modelMetaLoader.componentMetas ().values ())
			) {

				PluginSpec plugin =
					modelMeta.plugin ();

				try {

					modelRecordGeneratorProvider.provide (
						taskLogger)

						.plugin (
							plugin)

						.modelMeta (
							modelMeta)

						.generateRecord (
							taskLogger);

					statusCounters.recordSuccessCount ++;

				} catch (Exception exception) {

					taskLogger.errorFormatException (
						exception,
						"Error writing model record for %s",
						modelMeta.name ());

					statusCounters.recordErrorCount ++;

				}

				if (modelMeta.type ().record ()) {

					try {

						modelInterfacesGeneratorProvider.provide (
							taskLogger)

							.plugin (
								plugin)

							.modelMeta (
								modelMeta)

							.generateInterfaces (
								taskLogger);

						statusCounters.interfacesSuccessCount ++;

					} catch (Exception exception) {

						taskLogger.errorFormatException (
							exception,
							"Error writing model interfaces for %s",
							modelMeta.name ());

						statusCounters.interfacesErrorCount ++;

					}

				}

			}

			taskLogger.noticeFormat (
				"Successfully generated %s records and %s interfaces",
				integerToDecimalString (
					statusCounters.recordSuccessCount),
				integerToDecimalString (
					statusCounters.interfacesSuccessCount));

			if (
				statusCounters.recordErrorCount > 0
				|| statusCounters.interfacesErrorCount > 0
			) {

				taskLogger.errorFormat (
					"Aborting due to %s errors",
					integerToDecimalString (
						+ statusCounters.recordErrorCount
						+ statusCounters.interfacesErrorCount));

			}

		}

	}

	// data structures

	class StatusCounters {

		int recordSuccessCount = 0;
		int recordErrorCount = 0;

		int interfacesSuccessCount = 0;
		int interfacesErrorCount = 0;

	}

}
