package wbs.platform.media.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaTypeObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("mediaFixtureProvider")
public
class MediaFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaTypeObjectHelper mediaTypeHelper;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

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

			createMenuItems (
				taskLogger);

			createTextMediaTypes (
				taskLogger);

			createImageMediaTypes (
				taskLogger);

			createVideoMediaTypes (
				taskLogger);

		}

	}

	// private implementation

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"internal"))

				.setCode (
					"media_type")

				.setName (
					"Media type")

				.setDescription (
					"")

				.setLabel (
					"Media types")

				.setTargetPath (
					"/mediaTypes")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createTextMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createTextMediaTypes");

		) {

			createMediaType (
				transaction,
				"text/plain",
				"Plain text",
				"txt");

			transaction.commit ();

		}

	}

	private
	void createImageMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createImageMediaTypes");

		) {

			createMediaType (
				transaction,
				"image/jpeg",
				"JPEG image",
				"jpg");

			createMediaType (
				transaction,
				"image/gif",
				"GIF image",
				"git");

			createMediaType (
				transaction,
				"image/png",
				"PNG image",
				"png");

			createMediaType ( // TODO surely this is not right?!?
				transaction,
				"image/mp4",
				"MPEG-4 image",
				"mp4");

			transaction.commit ();

		}

	}

	private
	void createVideoMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createVideoMediaTypes");

		) {

			createMediaType (
				transaction,
				"video/3gpp",
				"3GPP video",
				"3gp");

			createMediaType (
				transaction,
				"video/mpeg",
				"MPEG video",
				"3gp");

		}

	}

	private
	void createMediaType (
			@NonNull Transaction parentTransaction,
			@NonNull String mimeType,
			@NonNull String description,
			@NonNull String extension) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMediaType");

		) {

			mediaTypeHelper.insert (
				transaction,
				mediaTypeHelper.createInstance ()

				.setMimeType (
					mimeType)

				.setDescription (
					description)

				.setExtension (
					extension)

			);

		}

	}

}
