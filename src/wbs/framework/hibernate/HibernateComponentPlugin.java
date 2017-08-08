package wbs.framework.hibernate;

import static wbs.utils.collection.IterableUtils.iterableChainArguments;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("hibernateComponentPlugin")
public
class HibernateComponentPlugin
	implements ComponentPlugin {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerHibernateLayerComponents");

		) {

			iterableChainArguments (
				plugin.models ().models (),
				plugin.models ().compositeTypes ()
			).forEach (
				projectModelSpec ->
					registerDaoHibernate (
						taskLogger,
						componentRegistry,
						projectModelSpec));

		}

	}

	// private implementation

	private
	void registerDaoHibernate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec pluginModelSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerDaoHibernate");

		) {

			String daoComponentName =
				stringFormat (
					"%sDao",
					pluginModelSpec.name ());

			String daoClassName =
				stringFormat (
					"%s.model.%sDao",
					pluginModelSpec.plugin ().packageName (),
					capitalise (
						pluginModelSpec.name ()));

			boolean gotDaoClass;

			try {

				Class.forName (
					daoClassName);

				gotDaoClass = true;

			} catch (ClassNotFoundException exception) {

				gotDaoClass = false;

			}

			String daoHibernateClassName =
				stringFormat (
					"%s.hibernate.%sDaoHibernate",
					pluginModelSpec.plugin ().packageName (),
					capitalise (
						pluginModelSpec.name ()));

			Class<?> daoHibernateClass = null;
			boolean gotDaoHibernateClass;

			try {

				daoHibernateClass =
					Class.forName (
						daoHibernateClassName);

				gotDaoHibernateClass = true;

			} catch (ClassNotFoundException exception) {

				gotDaoHibernateClass = false;

			}

			if (
				! gotDaoClass
				&& ! gotDaoHibernateClass
			) {
				return;
			}

			if (
				! gotDaoClass
				|| ! gotDaoHibernateClass
			) {

				taskLogger.errorFormat (
					"DAO methods or implementation missing for %s in %s",
					pluginModelSpec.name (),
					pluginModelSpec.plugin ().name ());

				return;

			}

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					daoComponentName)

				.componentClass (
					daoHibernateClass)

				.scope (
					"singleton")

			);

			return;

		}

	}

}
