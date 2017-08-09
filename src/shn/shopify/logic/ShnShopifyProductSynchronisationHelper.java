package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElement;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElement;
import static wbs.utils.collection.CollectionUtils.listThirdElement;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldRequest;
import wbs.integrations.shopify.apiclient.product.ShopifyProductApiClient;
import wbs.integrations.shopify.apiclient.product.ShopifyProductImageRequest;
import wbs.integrations.shopify.apiclient.product.ShopifyProductImageResponse;
import wbs.integrations.shopify.apiclient.product.ShopifyProductOptionRequest;
import wbs.integrations.shopify.apiclient.product.ShopifyProductRequest;
import wbs.integrations.shopify.apiclient.product.ShopifyProductResponse;
import wbs.integrations.shopify.apiclient.product.ShopifyProductVariantRequest;
import wbs.integrations.shopify.apiclient.product.ShopifyProductVariantResponse;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.logic.MediaLogic;

import shn.product.logic.ShnProductLogic;
import shn.product.model.ShnProductImageRec;
import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueRec;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifyProductSynchronisationHelper")
public
class ShnShopifyProductSynchronisationHelper
	implements ShnShopifySynchronisationHelper <
		ShnProductRec,
		ShopifyProductRequest,
		ShopifyProductResponse
	> {

	// singleton dependecies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	ShnProductObjectHelper productHelper;

	@SingletonDependency
	ShnProductLogic productLogic;

	@SingletonDependency
	ShopifyProductApiClient productApiClient;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	@SingletonDependency
	WbsConfig wbsConfig;

	// details

	@Override
	public
	ObjectHelper <ShnProductRec> objectHelper () {
		return productHelper;
	}

	@Override
	public
	String friendlyNameSingular () {
		return "product";
	}

	@Override
	public
	String friendlyNamePlural () {
		return "products";
	}

	@Override
	public
	Long getShopifyId (
			@NonNull ShnProductRec localItem) {

		return localItem.getShopifyId ();

	}

	@Override
	public
	Boolean getShopifyNeedsSync (
			@NonNull ShnProductRec localItem) {

		return localItem.getShopifyNeedsSync ();

	}

	@Override
	public
	void setShopifyNeedsSync (
			@NonNull ShnProductRec localItem,
			@NonNull Boolean value) {

		localItem.setShopifyNeedsSync (
			value);

	}

	@Override
	public
	String eventCode (
			@NonNull EventType eventType) {

		switch (eventType) {

		case create:

			return stringFormat (
				"shopping_nation_product_created_in_shopify");

		case update:

			return stringFormat (
				"shopping_nation_product_updated_in_shopify");

		case remove:

			return stringFormat (
				"shopping_nation_product_removed_in_shopify");

		default:

			throw shouldNeverHappen ();

		}

	}

	// public implementation

	@Override
	public
	Optional <ShnProductRec> findLocalItem (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItem");

		) {

			Optional <ShnProductRec> productOptional =
				productHelper.find (
					transaction,
					id);

			if (
				optionalIsNotPresent (
					productOptional)
			) {
				return optionalAbsent ();
			}

			ShnProductRec product =
				optionalGetRequired (
					productOptional);

			if (product.getDeleted ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				product);

		}

	}

	@Override
	public
	List <ShnProductRec> findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			return productHelper.findNotDeleted (
				transaction);

		}

	}

	@Override
	public
	List <ShopifyProductResponse> findRemoteItems (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRemoteItems");

		) {

			return productApiClient.listAll (
				transaction,
				credentials);

		}

	}

	@Override
	public
	void removeItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			productApiClient.remove (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyProductResponse createRemoteItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return productApiClient.create (
				transaction,
				credentials,
				request);

		}

	}

	@Override
	public
	ShopifyProductResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return productApiClient.update (
				transaction,
				credentials,
				request);

		}

	}

	@Override
	public
	List <String> compareItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyProductResponse remoteProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareItem");

		) {

			return ImmutableList.<String> builder ()

				.addAll (
					shopifyLogic.compareAttributes (
						transaction,
						connection,
						productAttributes,
						localProduct,
						remoteProduct))

				.addAll (
					shopifyLogic.compareCollection (
						transaction,
						connection,
						variantAttributes,
						ImmutableList.copyOf (
							localProduct.getVariants ()),
						remoteProduct.variants (),
						"variant",
						"variants"))

				.addAll (
					shopifyLogic.compareCollection (
						transaction,
						connection,
						imageAttributes,
						localProduct.getImages (),
						remoteProduct.images (),
						"image",
						"images"))

				.build ()

			;

		}

	}

	@Override
	public
	ShopifyProductRequest localToRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct,
			@NonNull Boolean create) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localToRequest");

		) {

			ShopifyProductRequest request =
				shopifyLogic.createRequest (
					transaction,
					connection,
					productAttributes,
					localProduct,
					ShopifyProductRequest.class)

				.images (
					shopifyLogic.createRequestCollection (
						transaction,
						connection,
						imageAttributes,
						localProduct.getImagesNotDeleted (),
						ShopifyProductImageRequest.class))

				.variants (
					shopifyLogic.createRequestCollection (
						transaction,
						connection,
						variantAttributes,
						localProduct.getVariantsNotDeleted (),
						ShopifyProductVariantRequest.class))

				.options (
					productRequestOptions (
						transaction,
						localProduct))

			;

			if (create) {

				request.metafields (
					ImmutableList.of (

					ShopifyMetafieldRequest.of (
						"shn-backend",
						"type",
						"product"),

					ShopifyMetafieldRequest.of (
						"shn-backend",
						"local-id",
						localProduct.getId ())

				));

			}

			return request;

		}

	}

	@Override
	public
	void updateLocalItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyProductResponse remoteProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"saveShopifyData");

		) {

			localProduct

				.setShopifyId (
					remoteProduct.id ())

				.setShopifyUpdatedAt (
					Instant.parse (
						remoteProduct.updatedAt ()))

			;

			for (
				long imageIndex = 0;
				imageIndex < collectionSize (
					localProduct.getImages ());
				imageIndex ++
			) {

				ShnProductImageRec localImage =
					listItemAtIndexRequired (
						localProduct.getImages (),
						imageIndex);

				ShopifyProductImageResponse remoteImage =
					listItemAtIndexRequired (
						remoteProduct.images (),
						imageIndex);

				localImage

					.setShopifySrc (
						remoteImage.src ())

					.setShopifyId (
						remoteImage.id ())

					.setShopifyCreatedAt (
						Instant.parse (
							remoteImage.createdAt ()))

					.setShopifyUpdatedAt (
						Instant.parse (
							remoteImage.updatedAt ()))

				;

			}

			List <ShnProductVariantRec> localVariantsOrdered =
				ImmutableList.copyOf (
					localProduct.getVariants ());

			for (
				long variantIndex = 0;
				variantIndex < collectionSize (
					localVariantsOrdered);
				variantIndex ++
			) {

				ShnProductVariantRec localVariant =
					listItemAtIndexRequired (
						localVariantsOrdered,
						variantIndex);

				ShopifyProductVariantResponse remoteVariant =
					listItemAtIndexRequired (
						remoteProduct.variants (),
						variantIndex);

				localVariant

					.setShopifyId (
						remoteVariant.id ())

					.setShopifyCreatedAt (
						Instant.parse (
							remoteVariant.createdAt ()))

					.setShopifyUpdatedAt (
						Instant.parse (
							remoteVariant.updatedAt ()))

				;

			}

		}

	}

	// private implementation

	private
	List <ShopifyProductOptionRequest> productRequestOptions (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"productRequestOptions");

		) {

			// add options

			List <ShnProductVariantTypeRec> localVariantTypes =
				ImmutableList.copyOf (
					iterable (
						localProduct.getVariants ().stream ()

				.flatMap (
					localProductVariant ->
						localProductVariant.getVariantValues ().stream ())

				.map (
					localVariantValue ->
						localVariantValue.getType ())

				.distinct ()

				.sorted (
					Ordering.natural ().onResultOf (
						ShnProductVariantTypeRec::getCode))

				.iterator ()

			));

			if (
				collectionIsEmpty (
					localVariantTypes)
			) {
				return null;
			}

			ImmutableList.Builder <ShopifyProductOptionRequest>
				shopifyOptionsBuilder =
					ImmutableList.builder ();

			for (
				ShnProductVariantTypeRec localVariantType
					: localVariantTypes
			) {

				shopifyOptionsBuilder.add (
					new ShopifyProductOptionRequest ()

					.name (
						localVariantType.getPublicTitle ())

				);

			}

			return shopifyOptionsBuilder.build ();

		}

	}

	// data

	ShopifySynchronisationAttribute.Factory <
		ShnProductVariantRec,
		ShopifyProductVariantRequest,
		ShopifyProductVariantResponse
	> variantAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductVariantRec,
		ShopifyProductVariantRequest,
		ShopifyProductVariantResponse
	>> variantAttributes =
		ImmutableList.of (

		// general

		variantAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductVariantResponse::id,
			ShnProductVariantRec::setShopifyId,
			ShnProductVariantRec::getShopifyId,
			ShopifyProductVariantRequest::id),

		variantAttributeFactory.sendSimple (
			String.class,
			"item number",
			ShnProductVariantRec::getItemNumber,
			ShopifyProductVariantRequest::sku,
			ShopifyProductVariantResponse::sku),

		variantAttributeFactory.sendSimple (
			String.class,
			"price",
			localVariant ->
				currencyLogic.formatSimple (
					localVariant.getDatabase ().getCurrency (),
					ifNull (
						localVariant.getPromotionalPrice (),
						localVariant.getShoppingNationPrice (),
						0l)),
			ShopifyProductVariantRequest::price,
			ShopifyProductVariantResponse::price),

		variantAttributeFactory.sendSimple (
			String.class,
			"compare at price",
			localVariant ->
				optionalOrNull (
					optionalMapRequired (
						optionalFromNullable (
							localVariant.getPromotionalPrice ()),
						promotionalPrice ->
							currencyLogic.formatSimple (
								localVariant.getDatabase ().getCurrency (),
								localVariant.getShoppingNationPrice ()))),
			ShopifyProductVariantRequest::compareAtPrice,
			ShopifyProductVariantResponse::compareAtPrice),

		variantAttributeFactory.sendSimple (
			Boolean.class,
			"taxable",
			localVariant -> true,
			ShopifyProductVariantRequest::taxable,
			ShopifyProductVariantResponse::taxable),

		variantAttributeFactory.sendSimple (
			String.class,
			"inventory management",
			localVariant -> "shopify",
			ShopifyProductVariantRequest::inventoryManagement,
			ShopifyProductVariantResponse::inventoryManagement),

		variantAttributeFactory.sendSimple (
			String.class,
			"inventory policy",
			localVariant -> "deny",
			ShopifyProductVariantRequest::inventoryPolicy,
			ShopifyProductVariantResponse::inventoryPolicy),

		// TODO inventory quantity (or not?)
		// TODO grams, weight, weightUnit (or not?)

		// delivery

		variantAttributeFactory.sendSimple (
			String.class,
			"fulfullment service",
			localVariant -> "manual",
			ShopifyProductVariantRequest::fulfillmentService,
			ShopifyProductVariantResponse::fulfillmentService),

		variantAttributeFactory.sendSimple (
			Boolean.class,
			"requires shipping",
			localVariant -> true,
			ShopifyProductVariantRequest::requiresShipping,
			ShopifyProductVariantResponse::requiresShipping),

		// options

		variantAttributeFactory.sendSimple (
			String.class,
			"option 1",
			localVariant ->
				optionalOr (
					listFirstElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle)),
				"Default Title"),
			ShopifyProductVariantRequest::option1,
			ShopifyProductVariantResponse::option1),

		variantAttributeFactory.sendSimple (
			String.class,
			"option 2",
			localVariant ->
				optionalOrNull (
					listSecondElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle))),
			ShopifyProductVariantRequest::option2,
			ShopifyProductVariantResponse::option2),

		variantAttributeFactory.sendSimple (
			String.class,
			"option 3",
			localVariant ->
				optionalOrNull (
					listThirdElement (
						iterableMapToList (
							productLogic.sortVariantValues (
								localVariant.getVariantValues ()),
							ShnProductVariantValueRec::getPublicTitle))),
			ShopifyProductVariantRequest::option3,
			ShopifyProductVariantResponse::option3),

		// miscellaneous

		variantAttributeFactory.receiveSimple (
			Instant.class,
			"created at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.createdAt ()),
			ShnProductVariantRec::setShopifyCreatedAt,
			ShnProductVariantRec::getShopifyCreatedAt),

		variantAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.updatedAt ()),
			ShnProductVariantRec::setShopifyUpdatedAt,
			ShnProductVariantRec::getShopifyUpdatedAt)

	);

	ShopifySynchronisationAttribute.Factory <
		ShnProductImageRec,
		ShopifyProductImageRequest,
		ShopifyProductImageResponse
	> imageAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductImageRec,
		ShopifyProductImageRequest,
		ShopifyProductImageResponse
	>> imageAttributes =
		ImmutableList.of (

		// general

		imageAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductImageResponse::id,
			ShnProductImageRec::setShopifyId,
			ShnProductImageRec::getShopifyId,
			ShopifyProductImageRequest::id),

		// TODO position?

		imageAttributeFactory.sendOnly (
			byte[].class,
			"attachment",
			context ->
				optionalOf (
					context.
						local ()
						.getOriginalMedia ()
						.getContent ()
						.getData ()),
			(context, value) ->
				context.remoteRequest ().attachment (
					bytesToBase64 (
						(byte[])
						optionalGetRequired (
							value)))),

		// TODO attachment

		imageAttributeFactory.receiveSimple (
			String.class,
			"src",
			ShopifyProductImageResponse::src,
			ShnProductImageRec::setShopifySrc,
			ShnProductImageRec::getShopifySrc),

		// miscellaneous

		imageAttributeFactory.receiveSimple (
			Instant.class,
			"created at",
			remoteImage ->
				Instant.parse (
					remoteImage.createdAt ()),
			ShnProductImageRec::setShopifyCreatedAt,
			ShnProductImageRec::getShopifyCreatedAt),

		imageAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteImage ->
				Instant.parse (
					remoteImage.updatedAt ()),
			ShnProductImageRec::setShopifyUpdatedAt,
			ShnProductImageRec::getShopifyUpdatedAt)

	);

	ShopifySynchronisationAttribute.Factory <
		ShnProductRec,
		ShopifyProductRequest,
		ShopifyProductResponse
	> productAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductRec,
		ShopifyProductRequest,
		ShopifyProductResponse
	>> productAttributes =
		ImmutableList.of (

		// general

		productAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyProductResponse::id,
			ShnProductRec::setShopifyId,
			ShnProductRec::getShopifyId,
			ShopifyProductRequest::id),

		productAttributeFactory.sendSimple (
			String.class,
			"title",
			ShnProductRec::getPublicTitle,
			ShopifyProductRequest::title,
			ShopifyProductResponse::title),

		productAttributeFactory.send (
			String.class,
			"body html",
			context ->
				optionalOf (
					shopifyLogic.productBodyHtml (
						context.transaction (),
						context.connection (),
						context.local ())),
			(context, value) ->
				context.remoteRequest ().bodyHtml (
					(String)
					optionalGetRequired (
						value)),
			context ->
				optionalFromNullable (
					context.remoteResponse ().bodyHtml ())),

		productAttributeFactory.sendSimple (
			String.class,
			"vendor",
			localProduct ->
				localProduct.getSupplier ().getPublicName (),
			ShopifyProductRequest::vendor,
			ShopifyProductResponse::vendor),

		productAttributeFactory.sendSimple (
			String.class,
			"product type",
			localProduct ->
				localProduct.getSubCategory ().getPublicTitle (),
			ShopifyProductRequest::productType,
			ShopifyProductResponse::productType)

	);

}
