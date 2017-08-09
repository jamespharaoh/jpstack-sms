package wbs.platform.background.logic;

import static wbs.utils.collection.CollectionUtils.collectionHasTwoItems;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.utils.string.StringUtils.underscoreToSpacesCapitalise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentMetaData;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.background.model.BackgroundProcessObjectHelper;
import wbs.platform.background.model.BackgroundProcessRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.utils.random.RandomLogic;

@SingletonComponent ("backgroundLogic")
public
class BackgroundLogicImplementation
	implements BackgroundLogic {

	// singleton components

	@SingletonDependency
	BackgroundProcessObjectHelper backgroundProcessHelper;

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	// prototype components

	@PrototypeDependency
	ComponentProvider <BackgroundProcessHelperImplementation>
		backgroundProcessHelperImplementationProvider;

	// state

	Map <String, String> backgroundProcessComponentNames =
		new HashMap<> ();

	// implementation

	@Override
	public
	BackgroundProcessHelper registerBackgroundProcess (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String backgroundProcessName,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerBackgroundProcess");

		) {

			List <String> backgroundProcessNameParts =
				stringSplitFullStop (
					backgroundProcessName);

			if (
				! collectionHasTwoItems (
					backgroundProcessNameParts)
			) {

				throw new RuntimeException (
					stringFormat (
						"Invalid background process name: %s",
						backgroundProcessName));

			}

			String parentTypeCode =
				listFirstElementRequired (
					backgroundProcessNameParts);

			String backgroundProcessCode =
				listSecondElementRequired (
					backgroundProcessNameParts);

			BackgroundProcessData backgroundProcessData =
				lookupBackgroundProcess (
					taskLogger,
					parentTypeCode,
					backgroundProcessCode);

			ComponentMetaData componentMetaData =
				componentManager.componentMetaData (
					component);

			ComponentDefinition componentDefinition =
				componentMetaData.definition ();

			if (
				stringNotEqualSafe (
					componentDefinition.scope (),
					"singleton")
			) {
				throw new RuntimeException ();
			}

			if (
				mapContainsKey (
					backgroundProcessComponentNames,
					backgroundProcessName)
			) {
				throw new RuntimeException ();
			}

			backgroundProcessComponentNames.put (
				backgroundProcessName,
				componentDefinition.name ());

			return backgroundProcessHelperImplementationProvider.provide (
				taskLogger)

				.parentTypeCode (
					parentTypeCode)

				.backgroundProcessCode (
					backgroundProcessCode)

				.backgroundProcessId (
					backgroundProcessData.id ())

				.backgroundProcessFrequency (
					backgroundProcessData.frequency ())

				.backgroundProcessFrequencyVariance (
					backgroundProcessData.frequency ().dividedBy (4l))

				.backgroundProcessDebugEnabled (
					backgroundProcessData.debugEnabled ())

			;

		}

	}

	private
	BackgroundProcessData lookupBackgroundProcess (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String parentTypeCode,
			@NonNull String backgroundProcessCode) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"lookupBackgroundProcess");

		) {

			ObjectTypeRec parentType =
				objectTypeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					hyphenToUnderscore (
						parentTypeCode));

			BackgroundProcessRec backgroundProcess =
				backgroundProcessHelper.findByCodeOrThrow (
					transaction,
					parentType,
					hyphenToUnderscore (
						backgroundProcessCode),
					() -> new RuntimeException (
						stringFormat (
							"Background process not found: %s.%s",
							parentTypeCode,
							backgroundProcessCode)));

			BackgroundProcessData backgroundProcessData =
				new BackgroundProcessData ()

				.id (
					backgroundProcess.getId ())

				.debugEnabled (
					backgroundProcess.getDebug ())

				.frequency (
					backgroundProcess.getFrequency ())

			;

			transaction.noticeFormat (
				"Found background process \"%s - %s\" with id %s",
					underscoreToSpacesCapitalise (
						parentType.getCode ()),
				backgroundProcess.getName (),
				integerToDecimalString (
					backgroundProcess.getId ()));

			return backgroundProcessData;

		}

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	private static
	class BackgroundProcessData {
		Long id;
		Boolean debugEnabled;
		Duration frequency;
	}

}
