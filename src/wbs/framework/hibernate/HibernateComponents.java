package wbs.framework.hibernate;

import static wbs.framework.utils.etc.MapUtils.mapToProperties;
import static wbs.framework.utils.etc.Misc.booleanToYesNo;

import java.util.Properties;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import org.hibernate.SessionFactory;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;

@SingletonComponent ("hibernateComponents")
public
class HibernateComponents {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <HibernateSessionFactoryBuilder>
		hibernateSessionFactoryBuilderProvider;

	// components

	@SingletonComponent ("hibernateSessionFactory")
	public
	SessionFactory hibernateSessionFactory () {

		Properties configProperties =
			mapToProperties (
				ImmutableMap.<String, String> builder ()

			.put (
				"hibernate.dialect",
				"org.hibernate.dialect.PostgreSQLDialect")

			.put (
				"hibernate.show_sql",
				booleanToYesNo (
					wbsConfig.database ().showSql ()))

			.put (
				"hibernate.format_sql",
				booleanToYesNo (
					wbsConfig.database ().formatSql ()))

			.put (
				"hibernate.connection.isolation",
				"8")

			.build ()

		);

		return hibernateSessionFactoryBuilderProvider.get ()

			.configProperties (
				configProperties)

			.build ();

	}

}
