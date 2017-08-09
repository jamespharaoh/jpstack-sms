package shn.product.fixture;

import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableFilterToSet;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.IterableUtils.iterableMapWithIndexToList;
import static wbs.utils.collection.MapUtils.mapFilterByKeyToMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.PlaceholderUtils.placeholderMapCurlyBraces;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotInSafe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.FixturesLogic;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.media.fixture.MediaFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;
import wbs.utils.random.RandomLogic;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductCategoryObjectHelper;
import shn.product.model.ShnProductImageObjectHelper;
import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductSubCategoryObjectHelper;
import shn.product.model.ShnProductVariantObjectHelper;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantTypeObjectHelper;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueRec;
import shn.supplier.model.ShnSupplierObjectHelper;

public
class ShnProductFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@SingletonDependency
	FixturesLogic fixturesLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaFixtureLogic mediaFixtureLogic;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnProductCategoryObjectHelper productCategoryHelper;

	@SingletonDependency
	ShnProductObjectHelper productHelper;

	@SingletonDependency
	ShnProductImageObjectHelper productImageHelper;

	@SingletonDependency
	ShnProductSubCategoryObjectHelper productSubCategoryHelper;

	@SingletonDependency
	ShnProductVariantObjectHelper productVariantHelper;

	@SingletonDependency
	ShnProductVariantTypeObjectHelper productVariantTypeHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	ShnSupplierObjectHelper supplierHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

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

			createProducts (
				taskLogger);

		}

	}

	// private implementation

	private
	void createProducts (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createProducts");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"local/test-data/shn-products.csv");

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
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productColumnNames,
						reader)
			) {

				Map <String, Object> mappingHints =
					ImmutableMap.<String, Object> builder ()

					.put (
						"currency",
						shnDatabase.getCurrency ())

					.build ()

				;

				// create product

				Map <String, String> productParams =
					placeholderMapCurlyBraces (
						productColumnMap,
						fixturesLogic.placeholderFunction (
							mappingHints,
							lineMap));

				ShnProductRec product =
					eventFixtureLogic.createRecordAndEvents (
						transaction,
						"SHN Product",
						productHelper,
						shnDatabase,
						productParams,
						emptySet ());

				// create product images

				product.setImages (
					iterableMapWithIndexToList (
						iterableFilter (
							randomLogic.shuffleToList (
								mediaFixtureLogic.testImages (
									transaction)),
							media ->
								randomLogic.randomBoolean (
									1l, 4l)),
						(index, media) ->
							productImageHelper.insert (
								transaction,
								productImageHelper.createInstance ()

					.setProduct (
						product)

					.setIndex (
						index)

					.setDescription (
						stringFormat (
							"Image %s",
							integerToDecimalString (
								index + 1)))

					.setOriginalMedia (
						media)

					.setActive (
						true)

				)));

				// create product variants

				Map <String, String> productVariantParams =
					placeholderMapCurlyBraces (
						productVariantColumnMap,
						fixturesLogic.placeholderFunction (
							mappingHints,
							lineMap));

				List <ShnProductVariantTypeRec> variantTypeSample =
					iterableFilterToList (
						productVariantTypeHelper.findAll (
							transaction),
						variantType ->
							randomLogic.randomBoolean (1l, 2l));

				List <Set <ShnProductVariantValueRec>> variantValueSample =
					iterableMapToList (
						variantTypeSample,
						variantType -> {

					for (;;) {

						Set <ShnProductVariantValueRec> variantValues =
							iterableFilterToSet (
								variantType.getValues (),
								variantValue ->
									randomLogic.randomBoolean (1l, 2l));

						if (
							collectionHasMoreThanOneElement (
								variantValues)
						) {
							return variantValues;
						}
					}

				});

				for (
					List <ShnProductVariantValueRec> variantValueCombination
						: Sets.cartesianProduct (
							variantValueSample)
				) {

					Map <String, String> productVariantCombinationParams =
						ImmutableMap.<String, String> builder ()

						.putAll (
							mapFilterByKeyToMap (
								productVariantParams,
								name ->
									stringNotInSafe (
										name,
										"itemNumber",
										"description",
										"publicTitle")))

						.put (
							"itemNumber",
							joinWithoutSeparator (
								mapItemForKeyRequired (
									productVariantParams,
									"itemNumber"),
								joinWithoutSeparator (
									iterableMap (
										variantValueCombination,
										ShnProductVariantValueRec::getCode))))

						.put (
							"description",
							joinWithSpace (
								mapItemForKeyRequired (
									productVariantParams,
									"description"),
								joinWithSpace (
									iterableMap (
										variantValueCombination,
										ShnProductVariantValueRec::getPublicTitle))))

						.put (
							"publicTitle",
							joinWithSpace (
								mapItemForKeyRequired (
									productVariantParams,
									"publicTitle"),
								joinWithSpace (
									iterableMap (
										variantValueCombination,
										ShnProductVariantValueRec::getPublicTitle))))

						.build ()

					;

					ShnProductVariantRec productVariant =
						eventFixtureLogic.createRecordAndEvents (
							transaction,
							"SHN Product",
							productVariantHelper,
							product,
							productVariantCombinationParams,
							emptySet ());

					productVariant.getVariantValues ().addAll (
						variantValueCombination);

				}

			}

			transaction.commit ();

		}

	}

	// data

	private final static
	List <String> productColumnNames =
		ImmutableList.of (
			"item-number",
			"supplier-name",
			"supplier-reference",
			"category-code",
			"sub-category-code",
			"description",
			"promo-code",
			"recommended-retail-price",
			"shopping-nation-price",
			"promo-price",
			"cost-price",
			"margin",
			"postage-and-packaging",
			"stock-quantity",
			"stock-value",
			"sold-quantity",
			"sales-retail",
			"sales-cost",
			"sales-rate",
			"last-date-aired",
			"first-date-aired",
			"active");

	private final static
	Map <String, String> productColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"item-number",
			"{item-number}")

		.put (
			"description",
			"{description}")

		.put (
			"subCategory",
			"shn.test.{category-code}.{sub-category-code}")

		.put (
			"public-title",
			"{description}")

		.put (
			"public-description",
			"{description}")

		.put (
			"public-contents",
			"(no contents data provided)")

		.put (
			"supplier",
			"shn.test.{codify:supplier-name}")

		.put (
			"active",
			"yes")

		.build ()

	;

	private final static
	Map <String, String> productVariantColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"itemNumber",
			"{item-number}")

		.put (
			"description",
			"{description}")

		.put (
			"publicTitle",
			"{description}")

		.put (
			"supplierReference",
			"{supplier-reference}")

		.put (
			"recommendedRetailPrice",
			"{currency:recommended-retail-price}")

		.put (
			"shoppingNationPrice",
			"{currency:shopping-nation-price}")

		.put (
			"promotionalPrice",
			"{currency:promo-price}")

		.put (
			"costPrice",
			"{currency:cost-price}")

		.put (
			"postageAndPackaging",
			"{currency:postage-and-packaging}")

		.put (
			"stockQuantity",
			"{stock-quantity}")

		.put (
			"active",
			"yes")

		.build ()

	;

}
