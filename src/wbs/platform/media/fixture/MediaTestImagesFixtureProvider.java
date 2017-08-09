package wbs.platform.media.fixture;

import static wbs.utils.io.FileUtils.fileReadBytes;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaTestImagesFixtureProvider")
public
class MediaTestImagesFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createFixtures");

		) {

			ImmutableMap.Builder <String, Long> testMediaIdsByNameBuilder =
				ImmutableMap.<String, Long> builder ();

			for (
				String testMediaName
					: testMediaNames
			) {

				MediaRec testMedia =
					mediaLogic.createMediaFromImageRequired (
						transaction,
						fileReadBytes (
							stringFormat (
								"binaries/test/%s.jpg",
								testMediaName)),
						"image/jpeg",
						stringFormat (
							"%s.jpg",
							testMediaName));


				testMediaIdsByNameBuilder.put (
					testMediaName,
					testMedia.getId ());

			}

			testMediaIdsByName =
				testMediaIdsByNameBuilder.build ();

			testMediaIds =
				ImmutableList.copyOf (
					testMediaIdsByName.values ());

			transaction.commit ();

		}

	}

	public final static
	List <String> testMediaNames =
		ImmutableList.of (
			"brian",
			"ermintrude",
			"dougal",
			"zebedee");

	public static
	Map <String, Long> testMediaIdsByName;

	public static
	List <Long> testMediaIds;

}
