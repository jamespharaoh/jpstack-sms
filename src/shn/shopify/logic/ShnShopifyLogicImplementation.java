package shn.shopify.logic;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.string.PlaceholderUtils.placeholderMapCurlyBraces;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.pluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.web.utils.HtmlUtils.htmlEncodeSimpleNewlineToBr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductRec;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifyLogicImplementation")
public
class ShnShopifyLogicImplementation
	implements ShnShopifyLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	String productBodyHtml (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec product) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"productDescriptionFromTemplate");

		) {

			if (

				isNotNull (
					product.getPublicContents ())

				&& stringIsNotEmpty (
					product.getPublicContents ().getText ())

			) {

				return placeholderMapCurlyBraces (
					connection.getProductDescriptionAndContentsTemplate ()
						.getText (),
					ImmutableMap.<String, String> builder ()

					.put (
						"description",
						ifNotNullThenElse (
							product.getPublicDescription (),
							() -> htmlEncodeSimpleNewlineToBr (
								product.getPublicDescription ().getText ()),
							() -> ""))

					.put (
						"contents",
						ifNotNullThenElse (
							product.getPublicContents (),
							() -> htmlEncodeSimpleNewlineToBr (
								product.getPublicContents ().getText ()),
							() -> ""))

					.build ()

				);

			} else {

				return placeholderMapCurlyBraces (
					connection.getProductDescriptionTemplate ().getText (),
					ImmutableMap.<String, String> builder ()

					.put (
						"description",
						ifNotNullThenElse (
							product.getPublicDescription (),
							() -> htmlEncodeSimpleNewlineToBr (
								product.getPublicDescription ().getText ()),
							() -> ""))

					.build ()

				);

			}

		}

	}

	@Override
	public <Local, RemoteRequest, RemoteResponse>
	List <String> compareAttributes (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			@NonNull Local local,
			@NonNull RemoteResponse remote) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareAttributes");

		) {

			ShopifySynchronisationAttribute.Context <
				Local,
				RemoteRequest,
				RemoteResponse
			> context =
				new ShopifySynchronisationAttribute.Context <
					Local,
					RemoteRequest,
					RemoteResponse
				> ()

				.transaction (
					transaction)

				.connection (
					connection)

				.local (
					local)

				.remoteResponse (
					remote)

			;

			List <String> errors =
				new ArrayList<> ();

			for (
				ShopifySynchronisationAttribute <
					Local,
					RemoteRequest,
					RemoteResponse
				> attribute
					: attributes
			) {

				if (! attribute.compare ()) {
					continue;
				}

				Optional <Object> localValue =
					attribute.localGetOperation ().apply (
						context);

				Optional <Object> remoteValue =
					attribute.remoteGetOperation ().apply (
						context);

				boolean equal =
					attribute.compareOperation ().test (
						localValue,
						remoteValue);

				if (! equal) {

					errors.add (
						stringFormat (
							"%s: local = \"%s\", remote = \"%s\"",
							attribute.friendlyName (),
							objectToString (
								optionalOr (
									localValue,
									"null")),
							objectToString (
								optionalOr (
									remoteValue,
									"null"))));

				}

			}

			return errors;

		}

	}

	@Override
	public <Local, RemoteRequest, RemoteResponse>
	List <String> compareCollection (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			@NonNull List <Local> localItems,
			@NonNull List <RemoteResponse> remoteItems,
			@NonNull String singularName,
			@NonNull String pluralName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareCollection");

		) {

			if (
				integerNotEqualSafe (
					collectionSize (
						localItems),
					collectionSize (
						remoteItems))
			) {

				return singletonList (
					stringFormat (
						"%s and %s",
						pluralise (
							collectionSize (
								localItems),
							stringFormat (
								"local %s",
								singularName),
							stringFormat (
								"remote %s",
								pluralName)),
						pluralise (
							collectionSize (
								remoteItems),
							stringFormat (
								"local %s",
								singularName),
							stringFormat (
								"remote %s",
								pluralName))));

			}

			List <String> errors =
				new ArrayList<> ();

			for (
				long index = 0;
				index < collectionSize (
					localItems);
				index ++
			) {

				Local localItem =
					listItemAtIndexRequired (
						localItems,
						index);

				RemoteResponse remoteItem =
					listItemAtIndexRequired (
						remoteItems,
						index);

				for (
					String error
						: compareAttributes (
							transaction,
							connection,
							attributes,
							localItem,
							remoteItem)
				) {

					errors.add (
						stringFormat (
							"%s %s %s",
							singularName,
							integerToDecimalString (
								index + 1),
							error));

				}

			}

			return errors;

		}

	}

	@Override
	public <Local, RemoteRequest, RemoteResponse>
	RemoteRequest createRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			@NonNull Local local,
			@NonNull Class <RemoteRequest> remoteRequestClass) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createRequest");

		) {

			RemoteRequest remote =
				classInstantiate (
					remoteRequestClass);

			ShopifySynchronisationAttribute.Context <
				Local,
				RemoteRequest,
				RemoteResponse
			> context =
				new ShopifySynchronisationAttribute.Context <
					Local,
					RemoteRequest,
					RemoteResponse
				> ()

				.transaction (
					transaction)

				.connection (
					connection)

				.local (
					local)

				.remoteRequest (
					remote)

			;

			for (
				ShopifySynchronisationAttribute <
					Local,
					RemoteRequest,
					RemoteResponse
				> attribute
					: attributes
			) {

				if (! attribute.send ()) {
					continue;
				}

				Optional <Object> attributeValue =
					attribute.localGetOperation ().apply (
						context);

				attribute.remoteSetOperation.accept (
					context,
					attributeValue);

			}

			return remote;

		}

	}

	@Override
	public <Local, RemoteRequest, RemoteResponse>
	List <RemoteRequest> createRequestCollection (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull List <ShopifySynchronisationAttribute <
				Local,
				RemoteRequest,
				RemoteResponse
			>> attributes,
			@NonNull Collection <Local> localItems,
			@NonNull Class <RemoteRequest> remoteRequestClass) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createCollection");

		) {

			return iterableMapToList (
				localItems,
				local ->
					createRequest (
						transaction,
						connection,
						attributes,
						local,
						remoteRequestClass));

		}

	}

}
