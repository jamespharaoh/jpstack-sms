package wbs.framework.fixtures;

import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoItems;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.LogicUtils.parseBooleanYesNoRequired;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.PropertyUtils.propertyClassForClass;
import static wbs.utils.etc.PropertyUtils.propertySetAuto;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.utils.time.TimeUtils.isoTimestampParseRequired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

@SingletonComponent ("fixtuesLogic")
public
class FixturesLogicImplementation
	implements FixturesLogic {

	// singleton dependencies

	@SingletonDependency
	List <FixtureMappingPlugin> fixtureMappingPlugins;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// state

	Map <String, FixtureMappingPlugin> fixtureMappingPluginsByName;

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

			fixtureMappingPluginsByName =
				mapWithDerivedKey (
					fixtureMappingPlugins,
					FixtureMappingPlugin::name);

		}

	}

	// public implementation

	@Override
	public
	Map <String, Object> resolveParams (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <?> objectHelper,
			@NonNull Map <String, String> unresolvedParams,
			@NonNull Set <String> ignoreParams,
			@NonNull Map <Class <?>, Function <String, ?>> recordLookups) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"resolveParams");

		) {

			ImmutableMap.Builder <String, Object> resolvedParamsBuilder =
				ImmutableMap.builder ();

			Class <?> objectClass =
				objectHelper.objectClass ();

			for (
				Map.Entry <String, String> unresolvedParamEntry
					: unresolvedParams.entrySet ()
			) {

				String nameHyphen =
					unresolvedParamEntry.getKey ();

				if (
					contains (
						ignoreParams,
						nameHyphen)
				) {
					continue;
				}

				String valueString =
					unresolvedParamEntry.getValue ();

				String nameCamel =
					hyphenToCamel (
						nameHyphen);

				Class <?> propertyType =
					propertyClassForClass (
						objectClass,
						nameCamel);

				if (
					classEqualSafe (
						propertyType,
						String.class)
				) {

					resolvedParamsBuilder.put (
						nameCamel,
						valueString);

				} else if (
					classEqualSafe (
						propertyType,
						Boolean.class)
				) {

					resolvedParamsBuilder.put (
						nameCamel,
						parseBooleanYesNoRequired (
							valueString));

				} else if (
					classEqualSafe (
						propertyType,
						Long.class)
				) {

					if (
						stringIsNotEmpty (
							valueString)
					) {

						resolvedParamsBuilder.put (
							nameCamel,
							parseIntegerRequired (
								valueString));

					}

				} else if (
					classEqualSafe (
						propertyType,
						Instant.class)
				) {

					if (
						stringIsNotEmpty (
							valueString)
					) {

						resolvedParamsBuilder.put (
							nameCamel,
							isoTimestampParseRequired (
								valueString));

					}

				} else if (
					isSubclassOf (
						Record.class,
						propertyType)
				) {

					Optional <Function <String, ?>> recordLookupOptional =
						mapItemForKey (
							recordLookups,
							propertyType);

					if (
						optionalIsPresent (
							recordLookupOptional)
					) {

						Function <String, ?> recordLookup =
							optionalGetRequired (
								recordLookupOptional);

						resolvedParamsBuilder.put (
							nameCamel,
							recordLookup.apply (
								valueString));

					} else {

						ObjectHelper <?> propertyHelper =
							objectManager.objectHelperForClassRequired (
								propertyType);

						resolvedParamsBuilder.put (
							nameCamel,
							propertyHelper.findByCodeRequired (
								transaction,
								GlobalId.root,
								stringSplitFullStop (
									valueString)));

					}

				} else {

					throw new ClassCastException (
						stringFormat (
							"Don't know how to set property type: %s",
							classNameFull (
								propertyType)));

				}

			}

			return resolvedParamsBuilder.build ();

		}

	}

	@Override
	public <Type extends Record <Type>>
	Type createRecord (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <Type> objectHelper,
			@NonNull Record <?> parent,
			@NonNull Map <String, Object> resolvedParams) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createRecord");

		) {

			if (! objectHelper.parentTypeIsFixed ()) {

				throw new IllegalArgumentException (
					stringFormat (
						"Can't create record type %s ",
						objectHelper.objectName (),
						"without fixed parent type"));

			}

			Type object =
				objectHelper.createInstance ();

			if (! objectHelper.isRooted ()) {

				objectHelper.setParent (
					object,
					parent);

			}

			resolvedParams.forEach (
				(name, value) ->
					propertySetAuto (
						object,
						name,
						value));

			return objectHelper.insert (
				transaction,
				object);

		}

	}

	@Override
	public
	Function <String, String> placeholderFunction (
			@NonNull Map <String, Object> hints,
			@NonNull Map <String, String> params) {

		return placeholder -> {

			List <String> placeholderParts =
				stringSplitColon (
					placeholder);

			String placeholderName =
				listLastItemRequired (
					placeholderParts);

			String placeholderValue =
				mapItemForKeyRequired (
					params,
					placeholderName);

			if (
				collectionHasOneItem (
					placeholderParts)
			) {

				doNothing ();

			} else if (
				collectionHasTwoItems (
					placeholderParts)
			) {

				String operation =
					listFirstElementRequired (
						placeholderParts);

				Optional <FixtureMappingPlugin> fixtureMappingPluginOptional =
					mapItemForKey (
						fixtureMappingPluginsByName,
						operation);

				if (
					optionalIsNotPresent (
						fixtureMappingPluginOptional)
				) {
					throw new IllegalArgumentException ();
				}

				FixtureMappingPlugin fixtureMappingPlugin =
					optionalGetRequired (
						fixtureMappingPluginOptional);

				placeholderValue =
					fixtureMappingPlugin.map (
						hints,
						placeholderValue);

			} else {

				throw new RuntimeException ();

			}

			return placeholderValue;

		};

	}

}
