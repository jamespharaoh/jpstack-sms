package wbs.framework.entity.helper;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.io.FileUtils.deleteDirectory;
import static wbs.utils.io.FileUtils.forceMkdir;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.model.CompositeModelBuilder;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.RecordModelBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("entityHelper")
public
class EntityHelperImplementation
	implements EntityHelper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <CompositeModelBuilder <?>> compositeModelBuilderProvider;

	@PrototypeDependency
	ComponentProvider <RecordModelBuilder <?>> recordModelBuilderProvider;

	// record model properties

	@Getter
	List <String> recordClassNames;

	@Getter
	List <Class <?>> recordClasses;

	@Getter
	List <Model <?>> recordModels;

	@Getter
	Map <Class <?>, Model <?>> recordModelsByClass;

	@Getter
	Map <String, Model <?>> recordModelsByName;

	// composite model properties

	@Getter
	List <String> compositeClassNames;

	@Getter
	List <Class <?>> compositeClasses;

	@Getter
	List <Model <?>> compositeModels;

	@Getter
	Map <Class <?>, Model <?>> compositeModelsByClass;

	@Getter
	Map <String, Model <?>> compositeModelsByName;

	// all model properties

	@Getter
	List <String> allModelClassNames;

	@Getter
	List <Class <?>> allModelClasses;

	@Getter
	List <Model <?>> allModels;

	@Getter
	Map <Class <?>, Model <?>> allModelsByClass;

	@Getter
	Map <String, Model <?>> allModelsByName;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			initClassNames (
				taskLogger);

			initClasses (
				taskLogger);

			initModels (
				taskLogger);

			initIndexes (
				taskLogger);

		}

	}

	void initClassNames (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initClassNames");

		) {

			ImmutableList.Builder <String> modelClassNamesBuilder =
				ImmutableList.builder ();

			ImmutableList.Builder <String> recordClassNamesBuilder =
				ImmutableList.builder ();

			ImmutableList.Builder <String> compositeClassNamesBuilder =
				ImmutableList.builder ();

			for (
				RecordSpec modelMeta
					: modelMetaLoader.modelMetas ().values ()
			) {

				PluginSpec plugin =
					modelMeta.plugin ();

				String modelClassName;

				if (modelMeta.type ().record ()) {

					modelClassName =
						stringFormat (
							"%s.model.%sRec",
							plugin.packageName (),
							capitalise (
								modelMeta.name ()));

					recordClassNamesBuilder.add (
						modelClassName);

				} else if (modelMeta.type ().component ()) {

					modelClassName =
						stringFormat (
							"%s.model.%s",
							plugin.packageName (),
							capitalise (
								modelMeta.name ()));

					compositeClassNamesBuilder.add (
						modelClassName);

				} else {

					throw shouldNeverHappen ();

				}

				modelClassNamesBuilder.add (
					modelClassName);

			}

			allModelClassNames =
				modelClassNamesBuilder.build ();

			recordClassNames =
				recordClassNamesBuilder.build ();

			compositeClassNames =
				compositeClassNamesBuilder.build ();

		}

	}

	void initClasses (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initClasses");

		) {

			ImmutableList.Builder <Class <?>> modelClassesBuilder =
				ImmutableList.builder ();

			ImmutableList.Builder <Class <?>> recordClassesBuilder =
				ImmutableList.builder ();

			for (
				String recordClassName
					: recordClassNames
			) {

				try {

					Class <?> recordClass =
						Class.forName (
							recordClassName);

					modelClassesBuilder.add (
						recordClass);

					recordClassesBuilder.add (
						recordClass);

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"No such record class %s",
						recordClassName);

				}

			}

			recordClasses =
				recordClassesBuilder.build ();

			ImmutableList.Builder <Class <?>> compositeClassesBuilder =
				ImmutableList.builder ();

			for (
				String compositeClassName
					: compositeClassNames
			) {

				try {

					Class <?> componentClass =
						Class.forName (
							compositeClassName);

					modelClassesBuilder.add (
						componentClass);

					compositeClassesBuilder.add (
						componentClass);

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"No such component class %s",
						compositeClassName);

				}

			}

			compositeClasses =
				compositeClassesBuilder.build ();

			allModelClasses =
				modelClassesBuilder.build ();

			taskLogger.makeException ();

		}

	}

	void initModels (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initModels");

		) {

			try {

				deleteDirectory (
					"work/model");

				forceMkdir (
					"work/model");

			} catch (RuntimeException exception) {

				taskLogger.errorFormat (
					"Error deleting contents of work/model: %s",
					exception.getMessage ());

			}

			ImmutableList.Builder <Model <?>> recordModelsBuilder =
				ImmutableList.builder ();

			ImmutableList.Builder <Model <?>> compositeModelsBuilder =
				ImmutableList.builder ();

			ImmutableList.Builder <Model <?>> allModelsBuilder =
				ImmutableList.builder ();

			int errors = 0;

			for (
				RecordSpec modelMeta
					: modelMetaLoader.modelMetas ().values ()
			) {

				Model <?> model;

				if (modelMeta.type ().record ()) {

					model =
						recordModelBuilderProvider.provide (
							taskLogger)

						.modelMeta (
							modelMeta)

						.build (
							taskLogger);

				} else if (modelMeta.type ().component ()) {

					model =
						compositeModelBuilderProvider.provide (
							taskLogger)

						.modelMeta (
							modelMeta)

						.build (
							taskLogger);

				} else {

					throw shouldNeverHappen ();

				}

				if (model == null) {

					errors ++;

					continue;

				}

				allModelsBuilder.add (
					model);

				if (modelMeta.type ().record ()) {

					recordModelsBuilder.add (
						model);

				} else if (modelMeta.type ().component ()) {

					compositeModelsBuilder.add (
						model);

				} else {

					throw shouldNeverHappen ();

				}

				String outputFilename =
					stringFormat (
						"work/model/%s.xml",
						camelToHyphen (
							model.objectName ()));

				try {

					new DataToXml ().writeToFile (
						outputFilename,
						model);

				} catch (Exception exception) {

					taskLogger.warningFormat (
						"Error writing %s",
						outputFilename);

				}

			}

			if (errors > 0)
				throw new RuntimeException ();

			allModels =
				allModelsBuilder.build ();

			recordModels =
				recordModelsBuilder.build ();

			compositeModels =
				compositeModelsBuilder.build ();

		}

	}

	private
	void initIndexes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initIndexes");

		) {

			recordModelsByClass =
				mapWithDerivedKey (
					recordModels,
					Model::objectClass);

			recordModelsByName =
				mapWithDerivedKey (
					recordModels,
					Model::objectName);

			compositeModelsByClass =
				mapWithDerivedKey (
					compositeModels,
					Model::objectClass);

			compositeModelsByName =
				mapWithDerivedKey (
					compositeModels,
					Model::objectName);

			allModelsByClass =
				mapWithDerivedKey (
					allModels,
					Model::objectClass);

			allModelsByName =
				mapWithDerivedKey (
					allModels,
					Model::objectName);

		}

	}

}
