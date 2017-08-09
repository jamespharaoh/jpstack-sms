package wbs.integrations.shopify.daemon;

import static wbs.utils.collection.IterableUtils.iterableFilterByClass;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.PropertyUtils.propertyClassForObject;
import static wbs.utils.etc.PropertyUtils.propertyGetSimple;
import static wbs.utils.etc.PropertyUtils.propertySetAuto;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;

import wbs.integrations.shopify.apiclient.ShopifyApiClient;
import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;
import wbs.integrations.shopify.apiclient.customcollection.ShopifyCustomCollectionApiClient;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldApiClient;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldResponse;
import wbs.integrations.shopify.apiclient.product.ShopifyProductApiClient;
import wbs.integrations.shopify.metamodel.ShopifySynchronisationSpec;
import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyCustomCollectionObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectType;
import wbs.integrations.shopify.model.ShopifyMetafieldObjectHelper;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;
import wbs.integrations.shopify.model.ShopifyMetafieldRec;
import wbs.integrations.shopify.model.ShopifyObjectHelper;
import wbs.integrations.shopify.model.ShopifyProductObjectHelper;
import wbs.integrations.shopify.model.ShopifyRecord;

import wbs.platform.daemon.ObjectDaemon;

@SingletonComponent ("shopifyEventSubjectProcessObjectDaemonHelper")
public
class ShopifyEventSubjectProcessObjectDaemonHelper
	implements ObjectDaemon <Long> {

	// singleton dependencies

	@SingletonDependency
	private
	Database database;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	private
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	ShopifyApiLogic shopifyApiLogic;

	@SingletonDependency
	private
	ShopifyCustomCollectionApiClient shopifyCustomCollectionApiClient;

	@SingletonDependency
	private
	ShopifyCustomCollectionObjectHelper shopifyCustomCollectionHelper;

	@SingletonDependency
	private
	ShopifyEventSubjectObjectHelper shopifyEventSubjectHelper;

	@SingletonDependency
	private
	ShopifyMetafieldApiClient shopifyMetafieldApiClient;

	@SingletonDependency
	private
	ShopifyMetafieldObjectHelper shopifyMetafieldHelper;

	@SingletonDependency
	private
	ShopifyProductApiClient shopifyProductApiClient;

	@SingletonDependency
	private
	ShopifyProductObjectHelper shopifyProductHelper;

	// details

	@Override
	public
	String backgroundProcessName () {
		return "shopify-event-subject.process";
	}

	@Override
	public
	String itemNameSingular () {
		return "Shopify event subject";
	}

	@Override
	public
	String itemNamePlural () {
		return "Shopify event subjects";
	}

	@Override
	public
	LogContext logContext () {
		return logContext;
	}

	// object daemon implementation

	@Override
	public
	List <Long> findObjectIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"findObjectIds");

		) {

			return shopifyEventSubjectHelper.findIdsPendingLimit (
				transaction,
				16384l);

		}

	}

	@Override
	public
	void processObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long eventSubjectId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processObject",
					keyEqualsDecimalInteger (
						"eventSubjectId",
						eventSubjectId));

		) {

			EventData eventData =
				getEventData (
					taskLogger,
					eventSubjectId);

			boolean remoteExists;

			switch (eventData.subjectType ()) {

			case collection:

				remoteExists =
					updateRecord (
						taskLogger,
						eventData,
						shopifyCustomCollectionHelper,
						shopifyCustomCollectionApiClient);

				if (remoteExists) {

					updateMetafieldRecords (
						taskLogger,
						eventData);

				}

				break;

			case product:

				remoteExists =
					updateRecord (
						taskLogger,
						eventData,
						shopifyProductHelper,
						shopifyProductApiClient);

				if (remoteExists) {

					updateMetafieldRecords (
						taskLogger,
						eventData);

				}

				break;

			default:

				taskLogger.warningFormat (
					"Ignoring event for unknown subject type: %s",
					enumNameHyphens (
						eventData.subjectType ()));

				remoteExists =
					true;

			}

			clearEventPending (
				taskLogger,
				eventData,
				remoteExists);

		}

	}

	// private implementation

	private
	EventData getEventData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long eventId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getEventSubjectType")

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventId);

			return new EventData ()

				.eventId (
					eventId)

				.subjectType (
					shopifyEventSubject.getSubjectType ())

				.subjectId (
					shopifyEventSubject.getSubjectId ())

				.credentials (
					shopifyApiLogic.getApiCredentials (
						transaction,
						shopifyEventSubject.getAccount ()))

			;

		}

	}

	private <
		RecordType extends ShopifyRecord <RecordType>,
		ResponseType extends ShopifyApiResponseItem
	>
	boolean updateRecord (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventData eventData,
			@NonNull ShopifyObjectHelper <RecordType> objectHelper,
			@NonNull ShopifyApiClient <?, ResponseType> apiClient) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"updateRecord");

		) {

			Optional <ResponseType> responseOptional =
				apiClient.get (
					taskLogger,
					eventData.credentials (),
					eventData.subjectId ());

			updateRecordLocal (
				taskLogger,
				eventData,
				objectHelper,
				responseOptional);

			return optionalIsPresent (
				responseOptional);

		}

	}

	private <
		RecordType extends ShopifyRecord <RecordType>,
		ResponseType extends ShopifyApiResponseItem
	>
	void updateRecordLocal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventData eventData,
			@NonNull ShopifyObjectHelper <RecordType> objectHelper,
			@NonNull Optional <ResponseType> responseOptional) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"updateRecord");

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventData.eventId ());

			if (! shopifyEventSubject.getPending ()) {
				return;
			}

			ShopifyAccountRec shopifyAccount =
				shopifyEventSubject.getAccount ();

			Optional <RecordType> localOptional =
				objectHelper.findByShopifyId (
					transaction,
					shopifyAccount,
					eventData.subjectId ());

			if (

				optionalIsNotPresent (
					responseOptional)

				&& optionalIsPresent (
					localOptional)

			) {

				// delete object

				RecordType local =
					optionalGetRequired (
						localOptional);

				local.setDeleted (
					true);

				// TODO events

			} else if (

				optionalIsPresent (
					responseOptional)

			) {

				// create or update object

				ResponseType response =
					optionalGetRequired (
						responseOptional);

				RecordType object;

				if (
					optionalIsPresent (
						localOptional)
				) {

					object =
						optionalGetRequired (
							localOptional);

				} else {

					object =
						objectHelper.createInstance ()

						.setAccount (
							shopifyAccount)

						.setShopifyId (
							response.id ())

					;

				}

				updateRecordLocalFields (
					transaction,
					objectHelper,
					response,
					object);

				if (
					optionalIsPresent (
						localOptional)
				) {

					objectHelper.update (
						transaction,
						object);

				} else {

					objectHelper.insert (
						transaction,
						object);

				}

			} else {

				doNothing ();

			}

			// commit transaction

			transaction.commit ();

		}

	}

	private
	void updateMetafieldRecords (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventData eventData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"updateMetafieldRecords");

		) {

			List <ShopifyMetafieldResponse> metafieldResponses =
				shopifyMetafieldApiClient.listByOwner (
					taskLogger,
					eventData.credentials (),
					mapItemForKeyRequired (
						eventSubjectTypeToMetafieldOwnerResource,
						eventData.subjectType ()),
					eventData.subjectId ());

			updateMetafieldRecordsLocal (
				taskLogger,
				eventData,
				metafieldResponses);

		}

	}

	private
	void updateMetafieldRecordsLocal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventData eventData,
			@NonNull List <ShopifyMetafieldResponse> metafieldResponses) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"updateMetafieldRecordsLocal");

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventData.eventId ());

			ShopifyAccountRec shopifyAccount =
				shopifyEventSubject.getAccount ();

			// process responses

			Map <Long, ShopifyMetafieldResponse> metafieldResponsesById =
				mapWithDerivedKey (
					metafieldResponses,
					ShopifyMetafieldResponse::id);

			for (
				ShopifyMetafieldResponse metafieldResponse
					: metafieldResponses
			) {

				Optional <ShopifyMetafieldRec> metafieldObjectOptional =
					shopifyMetafieldHelper.findByShopifyId (
						transaction,
						shopifyAccount,
						metafieldResponse.id ());

				ShopifyMetafieldRec metafieldObject;

				if (
					optionalIsPresent (
						metafieldObjectOptional)
				) {

					metafieldObject =
						optionalGetRequired (
							metafieldObjectOptional);

					metafieldObject.setDeleted (
						false);

				} else {

					metafieldObject =
						shopifyMetafieldHelper.createInstance ()

						.setAccount (
							shopifyAccount)

						.setShopifyId (
							metafieldResponse.id ())

					;

				}

				updateRecordLocalFields (
					transaction,
					shopifyMetafieldHelper,
					metafieldResponse,
					metafieldObject);

				if (
					optionalIsPresent (
						metafieldObjectOptional)
				) {

					shopifyMetafieldHelper.update (
						transaction,
						metafieldObject);

				} else {

					shopifyMetafieldHelper.insert (
						transaction,
						metafieldObject);

				}

			}

			// remove deleted

			List <ShopifyMetafieldRec> metafieldObjects =
				shopifyMetafieldHelper.findByOwner (
					transaction,
					shopifyAccount,
					mapItemForKeyRequired (
						eventSubjectTypeToMetafieldOwnerResource,
						eventData.subjectType ()),
					eventData.subjectId ());

			for (
				ShopifyMetafieldRec metafieldObject
					: metafieldObjects
			) {

				if (

					mapContainsKey (
						metafieldResponsesById,
						metafieldObject.getShopifyId ())

					|| metafieldObject.getDeleted ()

				) {
					continue;
				}

				metafieldObject.setDeleted (
					true);

				shopifyMetafieldHelper.update (
					transaction,
					metafieldObject);

			}

			// commit

			transaction.commit ();

		}

	}

	private <
		RecordType extends ShopifyRecord <RecordType>,
		ResponseType extends ShopifyApiResponseItem
	>
	void updateRecordLocalFields (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <RecordType> objectHelper,
			@NonNull ResponseType response,
			@NonNull RecordType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateRecordLocalFields");

		) {

			RecordSpec recordSpec =
				mapItemForKeyRequired (
					modelMetaLoader.recordSpecs (),
					objectHelper.objectName ());

			ShopifySynchronisationSpec synchronisationSpec =
				iterableOnlyItemRequired (
					iterableFilterByClass (
						recordSpec.children (),
						ShopifySynchronisationSpec.class));

			for (
				String fieldName
					: synchronisationSpec.scalarFieldNames ()
			) {

				Optional <Object> fieldValueOptional =
					optionalFromNullable (
						propertyGetSimple (
							response,
							hyphenToCamel (
								fieldName)));

				// perform conversion

				if (
					optionalIsPresent (
						fieldValueOptional)
				) {

					Class <?> targetClass =
						propertyClassForObject (
							object,
							hyphenToCamel (
								fieldName));

					fieldValueOptional =
						optionalOf (
							shopifyApiLogic.responseToLocal (
								transaction,
								optionalGetRequired (
									fieldValueOptional),
								targetClass));

				}

				// set local property

				propertySetAuto (
					object,
					hyphenToCamel (
						fieldName),
					optionalOrNull (
						fieldValueOptional));

				// TODO events

			}

			// TODO collections

		}

	}


	private
	void clearEventPending (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventData eventData,
			@NonNull Boolean remoteExists) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"clearEventPending");

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventData.eventId ());

			shopifyEventSubject

				.setDeleted (
					! remoteExists)

				.setPending (
					false)

				.setLastProcessTime (
					transaction.now ())

			;

			transaction.commit ();

		}

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	public final static
	class EventData {

		Long eventId;

		ShopifyEventSubjectType subjectType;
		Long subjectId;

		ShopifyApiClientCredentials credentials;

	}

	// data

	Map <ShopifyEventSubjectType, ShopifyMetafieldOwnerResource>
		eventSubjectTypeToMetafieldOwnerResource =
			ImmutableMap.<
				ShopifyEventSubjectType,
				ShopifyMetafieldOwnerResource
			> builder ()

		.put (
			ShopifyEventSubjectType.collection,
			ShopifyMetafieldOwnerResource.customCollection)

		.put (
			ShopifyEventSubjectType.product,
			ShopifyMetafieldOwnerResource.product)

		.build ()

	;

}
