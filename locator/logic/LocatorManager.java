package wbs.sms.locator.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
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

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	Database database;

	@Inject
	LocatorObjectHelper locatorHelper;

	@Inject
	LocatorLogObjectHelper locatorLogHelper;

	@Inject
	LocatorTypeObjectHelper locatorTypeHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	Map<String,Locator> locatorFactoriesByBeanName =
		Collections.emptyMap ();

	private final
	Map<Integer,Locator> locatorsByTypeId =
		new HashMap<Integer,Locator> ();

	private final
	ExecutorService executor =
		Executors.newCachedThreadPool ();

	@PostConstruct
	public
	void afterPropertiesSet () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

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

	private
	void logSuccess (
			int locatorLogId,
			LongLat longLat) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		LocatorLogRec locatorLog =
			locatorLogHelper.find (
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

	private
	void logFailure (
			int locatorLogId,
			String error,
			String errorCode) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		LocatorLogRec locatorLog =
			locatorLogHelper.find (
				locatorLogId);

		locatorLog
			.setSuccess (false)
			.setErrorText (textHelper.findOrCreate (error))
			.setErrorCode (errorCode);

		transaction.commit ();

	}

	public
	LongLat locate (
			int locatorId,
			int numberId,
			int serviceId,
			int affiliateId) {

		int locatorTypeId;
		int locatorLogId;

		String numberString;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		LocatorRec locatorRec =
			locatorHelper.find (
				locatorId);

		NumberRec number =
			numberHelper.find (
				numberId);

		ServiceRec service =
			serviceHelper.find (
				serviceId);

		AffiliateRec affiliate =
			affiliateHelper.find (
				affiliateId);

		locatorTypeId =
			locatorRec.getLocatorType ().getId ();

		numberString =
			number.getNumber ();

		LocatorLogRec locatorLog =
			locatorLogHelper.insert (
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
						locatorTypeId));

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
				locatorLogId,
				exception.getMessage (),
				exception.getErrorCode ());

			throw new LocatorException (
				exception);

		} catch (RuntimeException exception) {

			logFailure (
				locatorLogId,
				exception.getMessage (),
				null);

			throw new RuntimeException (
				exception);

		} catch (Error exception) {

			logFailure (
				locatorLogId,
				exception.getMessage (),
				null);

			throw new Error (exception);

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
			final int locatorId,
			final int numberId,
			final int serviceId,
			final int affiliateId,
			final Callback callback) {

		executor.execute (
			new Runnable () {

			@Override
			public
			void run () {

				try {

					LongLat longLat =
						locate (
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
