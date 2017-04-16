package wbs.sms.locator.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.locator.model.LocatorLogObjectHelper;
import wbs.sms.locator.model.LocatorLogRec;
import wbs.sms.locator.model.LocatorObjectHelper;
import wbs.sms.locator.model.LocatorRec;
import wbs.sms.locator.model.LocatorTypeObjectHelper;
import wbs.sms.locator.model.LocatorTypeRec;
import wbs.sms.locator.model.LongLat;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("locatorManager")
public
class LocatorManager {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	Map <String,Locator> locatorFactoriesByBeanName;

	@SingletonDependency
	LocatorObjectHelper locatorHelper;

	@SingletonDependency
	LocatorLogObjectHelper locatorLogHelper;

	@SingletonDependency
	LocatorTypeObjectHelper locatorTypeHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// state

	private final
	Map <Long, Locator> locatorsByTypeId =
		new HashMap<> ();

	private final
	ExecutorService executor =
		Executors.newCachedThreadPool ();

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		try (

			Transaction transaction =
				database.beginReadOnly (
					"LocatorManager.afterPropertiesSet ()",
					this);

		) {

			// index locators by type

			for (
				Map.Entry<String,Locator> locatorEntry
					: locatorFactoriesByBeanName.entrySet ()
			) {

				String beanName =
					locatorEntry.getKey ();

				Locator locator =
					locatorEntry.getValue ();

				for (
					String code
						: locator.getTypeCodes ()
				) {

					String[] codeParts =
						code.split ("\\.", 2);

					String parentTypeCode =
						codeParts [0];

					String locatorTypeCode =
						codeParts [1];

					ObjectTypeRec parentType =
						objectTypeHelper.findByCodeRequired (
							GlobalId.root,
							parentTypeCode);

					LocatorTypeRec locatorType =
						locatorTypeHelper.findByCodeRequired (
							parentType,
							locatorTypeCode);

					if (locatorsByTypeId.containsKey (locatorType.getId ())) {

						throw new RuntimeException (
							stringFormat (
								"Duplicated locator type code %s from %s",
								code,
								beanName));

					}

					locatorsByTypeId.put (
						locatorType.getId (),
						locator);

				}

			}

		}

	}

	private
	void logSuccess (
			@NonNull Long locatorLogId,
			@NonNull LongLat longLat) {

		try (

			Transaction transaction =
				database.beginReadWrite (
					"LocatorManager.logSuccess (locatorLogId, longLat)",
					this);

		) {

			LocatorLogRec locatorLog =
				locatorLogHelper.findRequired (
					locatorLogId);

			locatorLog

				.setEndTime (
					transaction.now ())

				.setSuccess (
					true)

				.setLongLat (
					longLat);

			transaction.commit ();

		}

	}

	private
	void logFailure (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long locatorLogId,
			@NonNull String error,
			@NonNull String errorCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"logFailure");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"LocatorManager.logFailure (locatorLogId, error, errorCode)",
					this);

		) {

			LocatorLogRec locatorLog =
				locatorLogHelper.findRequired (
					locatorLogId);

			locatorLog

				.setSuccess (
					false)

				.setErrorText (
					textHelper.findOrCreate (
						taskLogger,
						error))

				.setErrorCode (
					errorCode);

			transaction.commit ();

		}

	}

	public
	LongLat locate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long locatorId,
			@NonNull Long numberId,
			@NonNull Long serviceId,
			@NonNull Long affiliateId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"locate");

		Long locatorTypeId;
		Long locatorLogId;

		String numberString;

		try (

			Transaction transaction =
				database.beginReadWrite (
					"LocatorManager.locate (...)",
					this);

		) {

			LocatorRec locatorRec =
				locatorHelper.findRequired (
					locatorId);

			NumberRec number =
				numberHelper.findRequired (
					numberId);

			ServiceRec service =
				serviceHelper.findRequired (
					serviceId);

			AffiliateRec affiliate =
				affiliateHelper.findRequired (
					affiliateId);

			locatorTypeId =
				locatorRec.getLocatorType ().getId ();

			numberString =
				number.getNumber ();

			LocatorLogRec locatorLog =
				locatorLogHelper.insert (
					taskLogger,
					locatorLogHelper.createInstance ()

				.setLocator (
					locatorRec)

				.setNumber (
					number)

				.setStartTime (
					transaction.now ())

				.setService (
					service)

				.setAffiliate (
					affiliate)

			);

			locatorLogId =
				locatorLog.getId ();

			transaction.commit ();

			try {

				Locator locator =
					locatorsByTypeId.get (
						locatorTypeId);

				if (locator == null) {

					throw new RuntimeException (
						stringFormat (
							"Locator not found for type %s",
							integerToDecimalString (
								locatorTypeId)));

				}

				LongLat longLat =
					locator.lookup (
						locatorId,
						numberString);

				logSuccess (
					locatorLogId,
					longLat);

				return longLat;

			} catch (LocatorException exception) {

				logFailure (
					taskLogger,
					locatorLogId,
					exception.getMessage (),
					exception.getErrorCode ());

				throw new LocatorException (
					exception);

			} catch (RuntimeException exception) {

				logFailure (
					taskLogger,
					locatorLogId,
					exception.getMessage (),
					null);

				throw new RuntimeException (
					exception);

			} catch (Error exception) {

				logFailure (
					taskLogger,
					locatorLogId,
					exception.getMessage (),
					null);

				throw new Error (exception);

			}

		}

	}

	public static
	interface Callback {

		void success (
				LongLat longLat);

		void failure (
				RuntimeException throwable)
			throws RuntimeException;

	}

	public static
	class AbstractCallback
		implements Callback {

		@Override
		public
		void success (
				LongLat longLat) {

		}

		@Override
		public
		void failure (
				RuntimeException throwable)
			throws RuntimeException {

			throw throwable;

		}

	}

	public
	void locate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long locatorId,
			@NonNull Long numberId,
			@NonNull Long serviceId,
			@NonNull Long affiliateId,
			Callback callback) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"locate");

		executor.execute (
			new Runnable () {

			@Override
			public
			void run () {

				try {

					LongLat longLat =
						locate (
							taskLogger,
							locatorId,
							numberId,
							serviceId,
							affiliateId);

					callback.success (
						longLat);

				} catch (RuntimeException throwable) {

					callback.failure (
						throwable);

				}

			}

		});

	}

}
