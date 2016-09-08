package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.Iterables;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;

@Log4j
public
class ModelGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	Provider <ModelRecordGenerator> modelRecordGeneratorProvider;

	@PrototypeDependency
	Provider <ModelInterfacesGenerator> modelInterfacesGeneratorProvider;

	// implementation

	public
	void generateModels (
			@NonNull List <String> params) {

		log.info (
			stringFormat (
				"About to generate %s models",
				modelMetaLoader.modelMetas ().size ()));

		StatusCounters statusCounters =
			new StatusCounters ();

		for (
			ModelMetaSpec modelMeta
				: Iterables.concat (
					modelMetaLoader.modelMetas ().values (),
					modelMetaLoader.componentMetas ().values ())
		) {

			PluginSpec plugin =
				modelMeta.plugin ();

			try {

				modelRecordGeneratorProvider.get ()

					.plugin (
						plugin)

					.modelMeta (
						modelMeta)

					.generateRecord ();

				statusCounters.recordSuccessCount ++;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error writing model record for %s",
						modelMeta.name ()),
					exception);

				statusCounters.recordErrorCount ++;

			}

			if (modelMeta.type ().record ()) {

				try {

					modelInterfacesGeneratorProvider.get ()

						.plugin (
							plugin)

						.modelMeta (
							modelMeta)

						.generateInterfaces ();

					statusCounters.interfacesSuccessCount ++;

				} catch (Exception exception) {

					log.error (
						stringFormat (
							"Error writing model interfaces for %s",
							modelMeta.name ()),
						exception);

					statusCounters.interfacesErrorCount ++;

				}

			}

		}

		log.info (
			stringFormat (
				"Successfully generated %s records and %s interfaces",
				statusCounters.recordSuccessCount,
				statusCounters.interfacesSuccessCount));

		if (
			statusCounters.recordErrorCount > 0
			|| statusCounters.interfacesErrorCount > 0
		) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					+ statusCounters.recordErrorCount
					+ statusCounters.interfacesErrorCount));

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
