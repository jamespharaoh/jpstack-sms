package shn.supplier.fixture;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.supplier.model.ShnSupplierObjectHelper;
import shn.supplier.model.ShnSupplierRec;

public
class ShnSupplierFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	ShnSupplierObjectHelper shnSupplierHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			createSuppliers (
				taskLogger);

		}

	}

	private
	void createSuppliers (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createSuppliers");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-suppliers.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				String name =
					listItemAtIndexRequired (
						line,
						0l);

				String code =
					simplifyToCodeRequired (
						name);

				ShnSupplierRec supplier =
					shnSupplierHelper.insert (
						transaction,
						shnSupplierHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setName (
						name)

					.setDescription (
						name)

					.setPublicName (
						name)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Supplier",
					shnDatabase,
					supplier,
					ImmutableMap.<String, Object> builder ()

					.put (
						"description",
						name)

					.put (
						"publicName",
						name)

					.build ()
				);

			}

			transaction.commit ();

		}

	}

}
