package wbs.framework.component.scaffold;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("pluginManager")
public
class PluginManagerFactory
	implements ComponentFactory <PluginManager> {

	// singleton components

	@SingletonDependency
	BuildSpec buildSpec;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype components

	@PrototypeDependency
	ComponentProvider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// public implementation

	@Override
	public
	PluginManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			List <PluginSpec> plugins =
				loadPlugins (
					taskLogger);

			return buildReal (
				taskLogger,
				plugins);

		}

	}

	// private implementation

	private
	List <PluginSpec> loadPlugins (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadPlugins");

		) {

			ImmutableList.Builder <PluginSpec> pluginsBuilder =
				ImmutableList.builder ();

			DataFromXml pluginDataFromXml =
				dataFromXmlBuilderProvider.provide (
					taskLogger)

				.registerBuilderClasses (
					taskLogger,
					PluginApiModuleSpec.class,
					PluginBootstrapComponentSpec.class,
					PluginComponentSpec.class,
					PluginCompositeModelSpec.class,
					PluginConsoleModuleSpec.class,
					PluginCustomTypeSpec.class,
					PluginEnumTypeSpec.class,
					PluginFixtureSpec.class,
					PluginLayerSpec.class,
					PluginRecordModelSpec.class,
					PluginModelsSpec.class,
					PluginDependencySpec.class,
					PluginSpec.class)

				.build (
					taskLogger)

			;

			Set <String> pluginNames =
				new HashSet<> ();

			for (
				BuildPluginSpec buildPlugin
					: buildSpec.plugins ()
			) {

				String pluginPath =
					stringFormat (
						"/%s",
						buildPlugin.packageName ().replace (".", "/"),
						"/%s-plugin.xml",
						buildPlugin.name ());

				PluginSpec plugin =
					(PluginSpec)
					pluginDataFromXml.readClasspathRequired (
						taskLogger,
						pluginPath,
						ImmutableList.of (
							buildSpec));

				if (
					stringNotEqualSafe (
						buildPlugin.name (),
						plugin.name ())
				) {

					taskLogger.errorFormat (
						"Plugin name mismatch for %s ",
						pluginPath,
						"(should be %s ",
						buildPlugin.name (),
						"but was %s)",
						plugin.name ());

					continue;

				}

				if (
					contains (
						pluginNames,
						plugin.name ())
				) {

					taskLogger.errorFormat (
						"Duplicated plugin name: %s",
						plugin.name ());

					continue;

				}

				pluginsBuilder.add (
					plugin);

				pluginNames.add (
					plugin.name ());

			}

			taskLogger.makeException ();

			return pluginsBuilder.build ();

		}

	}

	private
	PluginManager buildReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <PluginSpec> plugins) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			verifyPluginDependencies (
				taskLogger,
				plugins);

			ImmutableList.Builder <PluginSpec> pluginsBuilder =
				ImmutableList.builder ();

			ImmutableMap.Builder <String, PluginRecordModelSpec>
				pluginRecordModelsByNameBuilder =
					ImmutableMap.builder ();

			ImmutableMap.Builder <String, PluginCompositeModelSpec>
				pluginCompositeModelsByNameBuilder =
					ImmutableMap.builder ();

			ImmutableMap.Builder <String, PluginEnumTypeSpec>
				pluginEnumTypesByNameBuilder =
					ImmutableMap.builder ();

			ImmutableMap.Builder <String, PluginCustomTypeSpec>
				pluginCustomTypesByNameBuilder =
					ImmutableMap.builder ();

			Set <String> donePluginNames =
				new HashSet<> ();

			List <PluginSpec> remainingPlugins =
				new LinkedList<> (
					plugins);

			Collections.sort (
				remainingPlugins);

			OUTER:
			for (;;) {

				ListIterator <PluginSpec> iterator =
					remainingPlugins.listIterator ();

				while (iterator.hasNext ()) {

					PluginSpec plugin =
						iterator.next ();

					if (
						! pluginDependenciesSatisfied (
							donePluginNames,
							plugin)
					) {
						continue;
					}

					pluginsBuilder.add (
						plugin);

					for (
						PluginRecordModelSpec pluginRecordModel
							: plugin.models ().models ()
					) {

						pluginRecordModelsByNameBuilder.put (
							pluginRecordModel.name (),
							pluginRecordModel);

					}

					for (
						PluginCompositeModelSpec pluginCompositeModel
							: plugin.models ().compositeTypes ()
					) {

						pluginCompositeModelsByNameBuilder.put (
							pluginCompositeModel.name (),
							pluginCompositeModel);

					}

					for (
						PluginEnumTypeSpec pluginEnumType
							: plugin.models ().enumTypes ()
					) {

						pluginEnumTypesByNameBuilder.put (
							pluginEnumType.name (),
							pluginEnumType);

					}

					for (
						PluginCustomTypeSpec pluginCustomType
							: plugin.models ().customTypes ()
					) {

						pluginCustomTypesByNameBuilder.put (
							pluginCustomType.name (),
							pluginCustomType);

					}

					donePluginNames.add (
						plugin.name ());

					iterator.remove ();

					taskLogger.debugFormat (
						"Resolved dependencies for plugin %s",
						plugin.name ());

					continue OUTER;

				}

				break;

			}

			if (! remainingPlugins.isEmpty ()) {

				for (
					PluginSpec plugin
						: remainingPlugins
				) {

					taskLogger.errorFormat (
						"Unable to resolve dependencies for plugin %s",
						plugin.name ());

				}

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						integerToDecimalString (
							remainingPlugins.size ())));

			}

			return new PluginManager ()

				.plugins (
					pluginsBuilder.build ())

				.pluginRecordModelsByName (
					pluginRecordModelsByNameBuilder.build ())

				.pluginCompositeModelsByName (
					pluginCompositeModelsByNameBuilder.build ())

				.pluginEnumTypesByName (
					pluginEnumTypesByNameBuilder.build ())

				.pluginCustomTypesByName (
					pluginCustomTypesByNameBuilder.build ())

			;

		}

	}

	private
	void verifyPluginDependencies (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <PluginSpec> plugins) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"verifyPluginDependencies");

		) {

			Set <String> allPluginNames =
				ImmutableSet.copyOf (
					iterableMap (
						plugins,
						PluginSpec::name));

			for (
				PluginSpec plugin
					: plugins
			) {

				List <String> missingDependencies =
					iterableFilterToList (
						iterableMap (
							plugin.pluginDependencies (),
							PluginDependencySpec::name),
						dependencyName ->
							doesNotContain (
								allPluginNames,
								dependencyName));

				if (
					collectionIsNotEmpty (
						missingDependencies)
				) {

					taskLogger.errorFormat (
						"Plugin %s missing dependencies: %s",
						plugin.name (),
						joinWithCommaAndSpace (
							missingDependencies));

				}

			}

			taskLogger.makeException ();

		}

	}

	boolean pluginDependenciesSatisfied (
			@NonNull Set <String> donePluginNames,
			@NonNull PluginSpec plugin) {

		for (
			PluginDependencySpec pluginDependency
				: plugin.pluginDependencies ()
		) {

			if (
				doesNotContain (
					donePluginNames,
					pluginDependency.name ())
			) {
				return false;
			}

		}

		return true;

	}

}
